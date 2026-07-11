package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.SuggestionDetailResponse;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionDetailMapper;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionMapper;
import com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 排班建议查询服务（读操作语义）。
 *
 * <p>边界规则：仅依赖自己模块的 Mapper（{@link AiScheduleSuggestionMapper}、{@link AiScheduleSuggestionDetailMapper}），
 * 不跨模块访问 dashboard、doctor、department 等模块的 Mapper。</p>
 */
@Service
@Transactional(readOnly = true)
public class AISuggestionQueryServiceImpl implements IAISuggestionQueryService {

	private final AiScheduleSuggestionMapper suggestionMapper;
	private final AiScheduleSuggestionDetailMapper detailMapper;

	public AISuggestionQueryServiceImpl(AiScheduleSuggestionMapper suggestionMapper,
			AiScheduleSuggestionDetailMapper detailMapper) {
		this.suggestionMapper = suggestionMapper;
		this.detailMapper = detailMapper;
	}

	@Override
	public long countSuggestionsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
		LambdaQueryWrapper<AiScheduleSuggestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.ge(AiScheduleSuggestion::getCreatedAt, startTime)
				.le(AiScheduleSuggestion::getCreatedAt, endTime);
		Long count = suggestionMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}

	@Override
	public List<AIScheduleSuggestionResponse> listSuggestions() {
		LambdaQueryWrapper<AiScheduleSuggestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.orderByDesc(AiScheduleSuggestion::getCreatedAt);
		List<AiScheduleSuggestion> suggestions = suggestionMapper.selectList(wrapper);
		if (suggestions == null || suggestions.isEmpty()) {
			return Collections.emptyList();
		}
		return suggestions.stream()
				.map(s -> toResponse(s, null))
				.collect(Collectors.toList());
	}

	@Override
	public List<AIScheduleSuggestionResponse> listSuggestionsByDepartment(Long departmentId) {
		if (departmentId == null) {
			return listSuggestions();
		}
		LambdaQueryWrapper<AiScheduleSuggestion> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(AiScheduleSuggestion::getDeptId, departmentId)
				.orderByDesc(AiScheduleSuggestion::getCreatedAt);
		List<AiScheduleSuggestion> suggestions = suggestionMapper.selectList(wrapper);
		if (suggestions == null || suggestions.isEmpty()) {
			return Collections.emptyList();
		}
		return suggestions.stream()
				.map(s -> toResponse(s, null))
				.collect(Collectors.toList());
	}

	@Override
	public AIScheduleSuggestionResponse getSuggestionDetail(Long suggestionId) {
		if (suggestionId == null) {
			throw new BusinessException(400, "建议ID不能为空");
		}
		AiScheduleSuggestion suggestion = suggestionMapper.selectById(suggestionId);
		if (suggestion == null) {
			throw new BusinessException(404, "AI 排班建议不存在");
		}
		List<AiScheduleSuggestionDetail> details = findDetailsBySuggestionId(suggestionId);
		return toResponse(suggestion, details);
	}

	@Override
	public List<SuggestionDetailResponse> listDetailsBySuggestionId(Long suggestionId) {
		if (suggestionId == null) {
			return Collections.emptyList();
		}
		List<AiScheduleSuggestionDetail> details = findDetailsBySuggestionId(suggestionId);
		return details.stream()
				.map(this::toDetailResponse)
				.collect(Collectors.toList());
	}

	@Override
	public AIScheduleSuggestionResponse toResponse(AiScheduleSuggestion suggestion,
			List<AiScheduleSuggestionDetail> details) {
		AIScheduleSuggestionResponse response = new AIScheduleSuggestionResponse();
		response.setSuggestionId(suggestion.getSuggestionId());
		response.setDepartmentId(suggestion.getDeptId());
		response.setStartDate(suggestion.getWorkDate());
		response.setEndDate(suggestion.getWorkDate());
		response.setStatus(suggestion.getStatus());
		if (details != null && !details.isEmpty()) {
			List<SuggestionDetailResponse> detailResponses = details.stream()
					.map(this::toDetailResponse)
					.collect(Collectors.toList());
			response.setDetails(detailResponses);
		} else {
			response.setDetails(Collections.emptyList());
		}
		return response;
	}

	private List<AiScheduleSuggestionDetail> findDetailsBySuggestionId(Long suggestionId) {
		LambdaQueryWrapper<AiScheduleSuggestionDetail> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(AiScheduleSuggestionDetail::getSuggestionId, suggestionId)
				.orderByAsc(AiScheduleSuggestionDetail::getScheduleDate)
				.orderByAsc(AiScheduleSuggestionDetail::getTimeSlot);
		List<AiScheduleSuggestionDetail> list = detailMapper.selectList(wrapper);
		return list != null ? list : Collections.emptyList();
	}

	private SuggestionDetailResponse toDetailResponse(AiScheduleSuggestionDetail detail) {
		SuggestionDetailResponse r = new SuggestionDetailResponse();
		r.setDetailId(detail.getDetailId());
		r.setDoctorId(detail.getDoctorId());
		r.setDoctorName(detail.getDoctorName());
		r.setScheduleDate(detail.getScheduleDate());
		r.setTimeSlot(detail.getTimeSlot());
		r.setMaxAppointments(detail.getMaxAppointments());
		r.setReason(detail.getReason());
		r.setStatus(detail.getStatus());
		return r;
	}
}
