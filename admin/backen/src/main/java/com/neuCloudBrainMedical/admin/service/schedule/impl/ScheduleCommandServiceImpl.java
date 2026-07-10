package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.ScheduleMapper;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

@Service
public class ScheduleCommandServiceImpl implements IScheduleCommandService {

	private static final String STATUS_ACTIVE = "可预约";
	private static final String SOURCE_MANUAL = "MANUAL";

	private final ScheduleMapper scheduleMapper;
	private final IDoctorQueryService doctorQueryService;
	private final IScheduleQueryService queryService;

	public ScheduleCommandServiceImpl(ScheduleMapper scheduleMapper,
			IDoctorQueryService doctorQueryService,
			IScheduleQueryService queryService) {
		this.scheduleMapper = scheduleMapper;
		this.doctorQueryService = doctorQueryService;
		this.queryService = queryService;
	}

	@Override
	@Transactional
	public ScheduleResponse createSchedule(ScheduleCreateRequest request) {
		return createScheduleWithSource(request, SOURCE_MANUAL);
	}

	@Override
	@Transactional
	public List<ScheduleResponse> batchCreateSchedule(ScheduleBatchCreateRequest request) {
		normalizeAndValidateBatch(request.getSchedules());
		return request.getSchedules().stream()
				.map(schedule -> createScheduleWithSource(schedule, SOURCE_MANUAL))
				.toList();
	}

	@Override
	@Transactional
	public ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request) {
		DoctorSchedule schedule = scheduleMapper.selectById(id);
		if (schedule == null) {
			throw new BusinessException(404, "排班不存在");
		}
		if (request.getTimeSlot() != null) {
			String timeSlot = ScheduleTimeSlotUtils.normalize(request.getTimeSlot());
			if (!timeSlot.equals(schedule.getTimePeriod())) {
				validateConflict(schedule.getDoctorId(), schedule.getWorkDate(), timeSlot);
				schedule.setTimePeriod(timeSlot);
				schedule.setStartTime(ScheduleTimeSlotUtils.defaultStartTime(timeSlot));
				schedule.setEndTime(ScheduleTimeSlotUtils.defaultEndTime(timeSlot));
			}
		}

		int newMax = schedule.getTotalQuota();
		int newCurrent = getCurrentAppointments(schedule);

		if (request.getMaxAppointments() != null) {
			newMax = request.getMaxAppointments();
		}
		if (request.getCurrentAppointments() != null) {
			newCurrent = request.getCurrentAppointments();
		}

		if (newMax < 0) {
			throw new BusinessException(400, "最大接诊量不能为负数");
		}
		if (newCurrent < 0 || newCurrent > newMax) {
			throw new BusinessException(400, "已预约数量不能为负数且不能超过最大接诊量");
		}

		if (request.getMaxAppointments() != null) {
			schedule.setTotalQuota(newMax);
		}
		schedule.setRemainQuota(newMax - newCurrent);

		if (request.getRegistrationFee() != null) {
			schedule.setRegistrationFee(request.getRegistrationFee());
		}
		if (request.getStatus() != null) {
			schedule.setStatus(request.getStatus());
		}
		schedule.setUpdatedAt(LocalDateTime.now());
		scheduleMapper.updateById(schedule);
		return toResponse(schedule);
	}

	@Override
	@Transactional
	public void cancelSchedule(Long id) {
		DoctorSchedule schedule = scheduleMapper.selectById(id);
		if (schedule == null) {
			throw new BusinessException(404, "排班不存在");
		}
		if (getCurrentAppointments(schedule) > 0) {
			throw new BusinessException(409, "该排班已有预约，不能删除，请改为停诊或调整接诊量");
		}
		scheduleMapper.deleteById(schedule.getScheduleId());
	}

	DoctorSchedule saveNewSchedule(ScheduleCreateRequest request, String source) {
		LocalDateTime now = LocalDateTime.now();
		DoctorSchedule schedule = new DoctorSchedule();
		schedule.setDoctorId(request.getDoctorId());
		schedule.setDeptId(request.getDepartmentId());
		schedule.setWorkDate(request.getScheduleDate());
		schedule.setTimePeriod(request.getTimeSlot());
		schedule.setTotalQuota(request.getMaxAppointments());
		schedule.setRemainQuota(request.getMaxAppointments());
		schedule.setStartTime(ScheduleTimeSlotUtils.defaultStartTime(request.getTimeSlot()));
		schedule.setEndTime(ScheduleTimeSlotUtils.defaultEndTime(request.getTimeSlot()));

		Map<Long, DoctorInfo> doctorMap = doctorQueryService.findDoctorInfoByIds(Collections.singleton(request.getDoctorId()));
		DoctorInfo doctor = doctorMap.get(request.getDoctorId());
		schedule.setRegistrationFee(ScheduleTimeSlotUtils.defaultFeeByTitle(doctor != null ? doctor.getTitle() : null));
		schedule.setStatus(STATUS_ACTIVE);
		schedule.setSource(source);
		schedule.setCreatedAt(now);
		schedule.setUpdatedAt(now);
		scheduleMapper.insert(schedule);
		return schedule;
	}

	void validateConflict(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DoctorSchedule::getDoctorId, doctorId);
		wrapper.eq(DoctorSchedule::getWorkDate, scheduleDate);
		wrapper.in(DoctorSchedule::getTimePeriod, ScheduleTimeSlotUtils.conflictValues(timeSlot));
		Long count = scheduleMapper.selectCount(wrapper);
		if (count != null && count > 0) {
			throw new BusinessException(409, buildConflictMessage(doctorId, scheduleDate, timeSlot));
		}
	}

	void normalizeAndValidateBatch(List<ScheduleCreateRequest> schedules) {
		if (schedules == null || schedules.isEmpty()) {
			throw new BusinessException(400, "请至少添加一条排班");
		}
		Set<String> requestKeys = new HashSet<>();
		for (ScheduleCreateRequest schedule : schedules) {
			String timeSlot = ScheduleTimeSlotUtils.normalize(schedule.getTimeSlot());
			schedule.setTimeSlot(timeSlot);
			String requestKey = buildScheduleKey(schedule.getDoctorId(), schedule.getScheduleDate(), timeSlot);
			if (!requestKeys.add(requestKey)) {
				throw new BusinessException(409, buildConflictMessage(
						schedule.getDoctorId(),
						schedule.getScheduleDate(),
						timeSlot));
			}
			validateConflict(schedule.getDoctorId(), schedule.getScheduleDate(), timeSlot);
		}
	}

	private int getCurrentAppointments(DoctorSchedule schedule) {
		return schedule.getTotalQuota() - schedule.getRemainQuota();
	}

	private String buildScheduleKey(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		return doctorId + "|" + scheduleDate + "|" + ScheduleTimeSlotUtils.normalize(timeSlot);
	}

	private String buildConflictMessage(Long doctorId, LocalDate scheduleDate, String timeSlot) {
		Map<Long, DoctorInfo> doctorMap = doctorQueryService.findDoctorInfoByIds(Collections.singleton(doctorId));
		DoctorInfo doctor = doctorMap.get(doctorId);
		String doctorLabel = doctor != null ? doctor.getDoctorNo() : "该医生";
		return "排班冲突：" + doctorLabel + "在" + scheduleDate + " " + ScheduleTimeSlotUtils.displayName(timeSlot) + "已安排，不能重复安排";
	}

	@Override
	@Transactional
	public ScheduleResponse createScheduleWithSource(ScheduleCreateRequest request, String source) {
		String timeSlot = ScheduleTimeSlotUtils.normalize(request.getTimeSlot());
		request.setTimeSlot(timeSlot);
		validateConflict(request.getDoctorId(), request.getScheduleDate(), timeSlot);
		DoctorSchedule schedule = saveNewSchedule(request, source);
		return toResponse(schedule);
	}

	private ScheduleResponse toResponse(DoctorSchedule schedule) {
		return queryService.toResponse(schedule);
	}
}
