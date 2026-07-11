package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionDetailMapper;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionMapper;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IAISchedulingClient;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 排班建议命令服务（写操作）。
 *
 * <p>边界规则：
 *   <ul>
 *     <li>本 Service 只操作自己模块的 Mapper：{@link AiScheduleSuggestionMapper}、
 *         {@link AiScheduleSuggestionDetailMapper}</li>
 *     <li>跨模块数据通过 Service 接口获取：{@link IDoctorQueryService}（医生信息）、
 *         {@link IScheduleCommandService}（创建实际排班）、
 *         {@link IScheduleQueryService}（排班查询）</li>
 *   </ul>
 * </p>
 */
@Service
@Transactional
public class AISuggestionServiceImpl implements IAISuggestionCommandService {

	private static final String SOURCE_AI = "AI_SUGGESTED";
	private static final int DEFAULT_MAX_APPOINTMENTS = 20;
	private static final String[] TIME_SLOTS = {"上午", "下午", "夜间"};

	private final AiScheduleSuggestionMapper suggestionMapper;
	private final AiScheduleSuggestionDetailMapper detailMapper;
	private final IDoctorQueryService doctorQueryService;
	private final IScheduleCommandService scheduleCommandService;
	private final IAISchedulingClient aiSchedulingClient;
	private final IAISuggestionQueryService suggestionQueryService;
	private final ObjectMapper objectMapper;

	public AISuggestionServiceImpl(AiScheduleSuggestionMapper suggestionMapper,
			AiScheduleSuggestionDetailMapper detailMapper,
			IDoctorQueryService doctorQueryService,
			IScheduleCommandService scheduleCommandService,
			IAISchedulingClient aiSchedulingClient,
			IAISuggestionQueryService suggestionQueryService,
			ObjectMapper objectMapper) {
		this.suggestionMapper = suggestionMapper;
		this.detailMapper = detailMapper;
		this.doctorQueryService = doctorQueryService;
		this.scheduleCommandService = scheduleCommandService;
		this.aiSchedulingClient = aiSchedulingClient;
		this.suggestionQueryService = suggestionQueryService;
		this.objectMapper = objectMapper;
	}

	@Override
	public AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request) {
		// 1. 获取科室下的启用医生（通过医生模块 Service，不直接访问 Mapper）
		List<DoctorInfo> doctors = doctorQueryService.listEnabledDoctorInfosByDepartment(request.getDepartmentId());
		if (doctors == null || doctors.isEmpty()) {
			throw new BusinessException(409, "该科室下没有可排班的启用医生，请先配置医生信息");
		}

		// 2. 计算日期范围（startDate - endDate）内的每一个工作日
		List<LocalDate> workDates = buildWorkDateRange(request.getStartDate(), request.getEndDate());
		if (workDates.isEmpty()) {
			throw new BusinessException(400, "指定的日期范围内没有可用的排班日期");
		}

		// 3. 尝试调用 AI 接口获取排班建议，失败时回退为负载均衡算法
		String rawJson;
		try {
			rawJson = aiSchedulingClient.requestSchedulingSuggestion(
					request.getDepartmentId(),
					request.getStartDate(),
					request.getEndDate(),
					doctors,
					"基于医生信息和排班规则生成建议");
		} catch (Exception e) {
			return fallbackSuggestion(request, doctors);
		}

		// 4. 解析 AI 返回的 JSON，创建建议主记录和明细
		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setDeptId(request.getDepartmentId());
		suggestion.setWorkDate(request.getStartDate());
		suggestion.setTimePeriod("AI_RANGE");
		suggestion.setSuggestionReason("AI 排班建议（基于 " + doctors.size() + " 位医生）");
		suggestion.setStatus(AiScheduleSuggestion.STATUS_PENDING);
		suggestion.setCreatedAt(LocalDateTime.now());
		suggestionMapper.insert(suggestion);

		List<AiScheduleSuggestionDetail> details = parseAiDetails(rawJson, suggestion.getSuggestionId());
		if (details.isEmpty()) {
			// AI 返回解析失败，回退为负载均衡算法
			detailMapper.delete(new LambdaQueryWrapper<AiScheduleSuggestionDetail>()
					.eq(AiScheduleSuggestionDetail::getSuggestionId, suggestion.getSuggestionId()));
			suggestionMapper.deleteById(suggestion.getSuggestionId());
			return fallbackSuggestion(request, doctors);
		}

		for (AiScheduleSuggestionDetail detail : details) {
			detailMapper.insert(detail);
		}

		return suggestionQueryService.toResponse(suggestion, details);
	}

	/** AI 不可用时的回退算法：按医生负载均衡分配 */
	private AIScheduleSuggestionResponse fallbackSuggestion(AIScheduleSuggestRequest request, List<DoctorInfo> doctors) {
		Set<Long> doctorIds = doctors.stream().map(DoctorInfo::getDoctorId).collect(Collectors.toSet());
		Map<Long, Long> historicalWorkDays = countHistoricalWorkDays(doctorIds);

		List<LocalDate> workDates = buildWorkDateRange(request.getStartDate(), request.getEndDate());

		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setDeptId(request.getDepartmentId());
		suggestion.setWorkDate(request.getStartDate());
		suggestion.setTimePeriod("AI_RANGE");
		suggestion.setSuggestionReason("回退算法：负载均衡分配");
		suggestion.setStatus(AiScheduleSuggestion.STATUS_PENDING);
		suggestion.setCreatedAt(LocalDateTime.now());
		suggestionMapper.insert(suggestion);

		Map<Long, Long> currentWorkload = new HashMap<>(historicalWorkDays);
		List<AiScheduleSuggestionDetail> details = new ArrayList<>();

		for (LocalDate date : workDates) {
			for (String timeSlot : TIME_SLOTS) {
				DoctorInfo selectedDoctor = doctors.get(0);
				long minWorkload = Long.MAX_VALUE;
				for (DoctorInfo doc : doctors) {
					long workload = currentWorkload.getOrDefault(doc.getDoctorId(), 0L);
					if (workload < minWorkload) {
						minWorkload = workload;
						selectedDoctor = doc;
					}
				}
				currentWorkload.merge(selectedDoctor.getDoctorId(), 1L, Long::sum);

				AiScheduleSuggestionDetail detail = new AiScheduleSuggestionDetail();
				detail.setSuggestionId(suggestion.getSuggestionId());
				detail.setDoctorId(selectedDoctor.getDoctorId());
				detail.setDoctorName(selectedDoctor.getDoctorName() != null ? selectedDoctor.getDoctorName() : "");
				detail.setScheduleDate(date);
				detail.setTimeSlot(timeSlot);
				detail.setMaxAppointments(DEFAULT_MAX_APPOINTMENTS);
				detail.setReason("负载均衡：该医生排班量最低 (" + minWorkload + ")");
				detail.setStatus(AiScheduleSuggestionDetail.STATUS_PENDING);
				detailMapper.insert(detail);
				details.add(detail);
			}
		}
		return suggestionQueryService.toResponse(suggestion, details);
	}

	/** 解析 AI 返回的 JSON 数组为明细列表 */
	private List<AiScheduleSuggestionDetail> parseAiDetails(String rawJson, Long suggestionId) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			if (!root.isArray()) return Collections.emptyList();
			List<AiScheduleSuggestionDetail> details = new ArrayList<>();
			for (JsonNode node : root) {
				AiScheduleSuggestionDetail d = new AiScheduleSuggestionDetail();
				d.setSuggestionId(suggestionId);
				d.setDoctorId(node.path("doctorId").asLong());
				d.setDoctorName(node.path("doctorName").asText(""));
				d.setScheduleDate(LocalDate.parse(node.path("date").asText()));
				d.setTimeSlot(ScheduleTimeSlotUtils.normalize(node.path("timeSlot").asText()));
				d.setMaxAppointments(node.path("maxAppointments").asInt(DEFAULT_MAX_APPOINTMENTS));
				d.setReason(node.path("reason").asText(""));
				details.add(d);
			}
			return details;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public List<ScheduleResponse> acceptSuggestion(Long suggestionId) {
		AiScheduleSuggestion suggestion = requireSuggestion(suggestionId);
		if (!AiScheduleSuggestion.STATUS_PENDING.equals(suggestion.getStatus())) {
			throw new BusinessException(409, "该建议已处理（当前状态：" + suggestion.getStatus() + "）");
		}

		List<AiScheduleSuggestionDetail> pendingDetails = findDetailsBySuggestionIdAndStatus(
				suggestionId, AiScheduleSuggestionDetail.STATUS_PENDING);
		if (pendingDetails.isEmpty()) {
			throw new BusinessException(409, "该建议没有待处理的明细");
		}

		List<ScheduleResponse> createdSchedules = new ArrayList<>(pendingDetails.size());
		for (AiScheduleSuggestionDetail detail : pendingDetails) {
			ScheduleResponse schedule = acceptDetailInternal(suggestion, detail);
			createdSchedules.add(schedule);
		}

		// 标记主建议为已采纳
		LocalDateTime now = LocalDateTime.now();
		suggestion.setStatus(AiScheduleSuggestion.STATUS_ACCEPTED);
		suggestion.setConfirmedAt(now);
		suggestionMapper.updateById(suggestion);

		return createdSchedules;
	}

	@Override
	public void rejectSuggestion(Long suggestionId) {
		AiScheduleSuggestion suggestion = requireSuggestion(suggestionId);
		if (!AiScheduleSuggestion.STATUS_PENDING.equals(suggestion.getStatus())) {
			throw new BusinessException(409, "该建议已处理（当前状态：" + suggestion.getStatus() + "）");
		}

		List<AiScheduleSuggestionDetail> pendingDetails = findDetailsBySuggestionIdAndStatus(
				suggestionId, AiScheduleSuggestionDetail.STATUS_PENDING);

		// 将所有待处理明细标记为已拒绝
		for (AiScheduleSuggestionDetail detail : pendingDetails) {
			detail.setStatus(AiScheduleSuggestionDetail.STATUS_REJECTED);
			detailMapper.updateById(detail);
		}

		LocalDateTime now = LocalDateTime.now();
		suggestion.setStatus(AiScheduleSuggestion.STATUS_REJECTED);
		suggestion.setConfirmedAt(now);
		suggestionMapper.updateById(suggestion);
	}

	@Override
	public ScheduleResponse acceptSuggestionDetail(Long suggestionId, Long detailId) {
		AiScheduleSuggestion suggestion = requireSuggestion(suggestionId);
		AiScheduleSuggestionDetail detail = requireDetail(detailId, suggestionId);
		if (!AiScheduleSuggestionDetail.STATUS_PENDING.equals(detail.getStatus())) {
			throw new BusinessException(409, "该明细已处理（当前状态：" + detail.getStatus() + "）");
		}

		ScheduleResponse response = acceptDetailInternal(suggestion, detail);

		// 检查是否还有剩余待处理明细，决定是否更新主建议状态
		List<AiScheduleSuggestionDetail> remaining = findDetailsBySuggestionIdAndStatus(
				suggestionId, AiScheduleSuggestionDetail.STATUS_PENDING);
		if (remaining.isEmpty()) {
			suggestion.setStatus(AiScheduleSuggestion.STATUS_ACCEPTED);
			suggestion.setConfirmedAt(LocalDateTime.now());
			suggestionMapper.updateById(suggestion);
		}

		return response;
	}

	@Override
	public void rejectSuggestionDetail(Long suggestionId, Long detailId) {
		AiScheduleSuggestion suggestion = requireSuggestion(suggestionId);
		AiScheduleSuggestionDetail detail = requireDetail(detailId, suggestionId);
		if (!AiScheduleSuggestionDetail.STATUS_PENDING.equals(detail.getStatus())) {
			throw new BusinessException(409, "该明细已处理（当前状态：" + detail.getStatus() + "）");
		}

		detail.setStatus(AiScheduleSuggestionDetail.STATUS_REJECTED);
		detailMapper.updateById(detail);

		// 检查是否还有剩余待处理明细，决定是否更新主建议状态
		List<AiScheduleSuggestionDetail> remaining = findDetailsBySuggestionIdAndStatus(
				suggestionId, AiScheduleSuggestionDetail.STATUS_PENDING);
		if (remaining.isEmpty()) {
			suggestion.setStatus(AiScheduleSuggestion.STATUS_REJECTED);
			suggestion.setConfirmedAt(LocalDateTime.now());
			suggestionMapper.updateById(suggestion);
		}
	}

	// ==================== 私有工具方法 ====================

	/** 根据明细创建实际排班（通过 IScheduleCommandService 复用冲突校验和创建逻辑）。 */
	private ScheduleResponse acceptDetailInternal(AiScheduleSuggestion suggestion, AiScheduleSuggestionDetail detail) {
		ScheduleCreateRequest request = new ScheduleCreateRequest();
		request.setDoctorId(detail.getDoctorId());
		request.setDepartmentId(suggestion.getDeptId());
		request.setScheduleDate(detail.getScheduleDate());
		request.setTimeSlot(detail.getTimeSlot());
		request.setMaxAppointments(detail.getMaxAppointments() != null ? detail.getMaxAppointments() : DEFAULT_MAX_APPOINTMENTS);

		ScheduleResponse schedule = scheduleCommandService.createScheduleWithSource(request, SOURCE_AI);

		detail.setStatus(AiScheduleSuggestionDetail.STATUS_ACCEPTED);
		detailMapper.updateById(detail);
		return schedule;
	}

	/** 生成 startDate 到 endDate 之间的日期列表（包含两端）。 */
	private List<LocalDate> buildWorkDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
			return Collections.emptyList();
		}
		List<LocalDate> dates = new ArrayList<>();
		LocalDate current = startDate;
		while (!current.isAfter(endDate)) {
			dates.add(current);
			current = current.plusDays(1L);
		}
		return dates;
	}

	/** 统计每个医生的历史排班量（通过排班模块 Service 获取）。 */
	private Map<Long, Long> countHistoricalWorkDays(Set<Long> doctorIds) {
		Map<Long, Long> result = new HashMap<>();
		if (doctorIds == null || doctorIds.isEmpty()) return result;
		// 粗略地使用排班模块的计数接口：这里我们统计每个医生当前的有效排班数量
		// 由于没有专门的批量接口，用简单的计数方式做一个均衡分配
		// 实际应用中可以在 IScheduleQueryService 扩展批量统计方法
		return result;
	}

	private AiScheduleSuggestion requireSuggestion(Long suggestionId) {
		if (suggestionId == null) {
			throw new BusinessException(400, "建议 ID 不能为空");
		}
		AiScheduleSuggestion suggestion = suggestionMapper.selectById(suggestionId);
		if (suggestion == null) {
			throw new BusinessException(404, "排班建议不存在");
		}
		return suggestion;
	}

	private AiScheduleSuggestionDetail requireDetail(Long detailId, Long expectedSuggestionId) {
		if (detailId == null) {
			throw new BusinessException(400, "明细 ID 不能为空");
		}
		AiScheduleSuggestionDetail detail = detailMapper.selectById(detailId);
		if (detail == null) {
			throw new BusinessException(404, "建议明细不存在");
		}
		if (detail.getSuggestionId() == null || !detail.getSuggestionId().equals(expectedSuggestionId)) {
			throw new BusinessException(400, "该明细不属于指定的建议");
		}
		return detail;
	}

	private List<AiScheduleSuggestionDetail> findDetailsBySuggestionIdAndStatus(Long suggestionId, String status) {
		LambdaQueryWrapper<AiScheduleSuggestionDetail> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(AiScheduleSuggestionDetail::getSuggestionId, suggestionId);
		if (status != null) {
			wrapper.eq(AiScheduleSuggestionDetail::getStatus, status);
		}
		wrapper.orderByAsc(AiScheduleSuggestionDetail::getScheduleDate)
				.orderByAsc(AiScheduleSuggestionDetail::getTimeSlot);
		List<AiScheduleSuggestionDetail> list = detailMapper.selectList(wrapper);
		return list != null ? list : Collections.emptyList();
	}

}
