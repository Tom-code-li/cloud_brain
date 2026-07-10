package com.neuCloudBrainMedical.admin.service.schedule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleRegistrationResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.entity.Registration;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.ScheduleMapper;
import com.neuCloudBrainMedical.admin.mapper.biz.RegistrationMapper;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排班查询服务（读操作）。
 *
 * <p>边界规则：
 *   <ul>
 *     <li>本 Service 只操作自己模块的 Mapper：{@link ScheduleMapper}、{@link RegistrationMapper}</li>
 *     <li>跨模块数据通过 Service 接口获取：{@link IDoctorQueryService}（医生信息）、
 *         {@link IDepartmentQueryService}（科室信息）、
 *         {@link IUserQueryService}（患者用户信息）</li>
 *   </ul>
 * </p>
 */
@Service
public class ScheduleQueryServiceImpl implements IScheduleQueryService {

	private final ScheduleMapper scheduleMapper;
	private final RegistrationMapper registrationMapper;
	private final IDoctorQueryService doctorQueryService;
	private final IDepartmentQueryService departmentQueryService;
	private final IUserQueryService userQueryService;

	public ScheduleQueryServiceImpl(ScheduleMapper scheduleMapper,
			RegistrationMapper registrationMapper,
			IDoctorQueryService doctorQueryService,
			IDepartmentQueryService departmentQueryService,
			IUserQueryService userQueryService) {
		this.scheduleMapper = scheduleMapper;
		this.registrationMapper = registrationMapper;
		this.doctorQueryService = doctorQueryService;
		this.departmentQueryService = departmentQueryService;
		this.userQueryService = userQueryService;
	}

	@Override
	public List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate) {
		// 自动将已过期的可预约排班标记为"已过期"
		expireOutdatedSchedules();

		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		if (departmentId != null) {
			wrapper.eq(DoctorSchedule::getDeptId, departmentId);
		}
		if (startDate != null) {
			wrapper.ge(DoctorSchedule::getWorkDate, startDate);
		}
		if (endDate != null) {
			wrapper.le(DoctorSchedule::getWorkDate, endDate);
		}
		wrapper.orderByAsc(DoctorSchedule::getWorkDate).orderByAsc(DoctorSchedule::getScheduleId);
		List<DoctorSchedule> schedules = scheduleMapper.selectList(wrapper);
		if (schedules == null || schedules.isEmpty()) return Collections.emptyList();

		return schedulesToResponses(schedules);
	}

	@Override
	public ScheduleResponse getScheduleDetail(Long id) {
		DoctorSchedule schedule = scheduleMapper.selectById(id);
		if (schedule == null) {
			throw new BusinessException(404, "排班不存在");
		}
		return toResponse(schedule);
	}

	/**
	 * 将单个排班 Entity 转换为 DTO（带医生 / 科室 / 挂号量关联信息）。
	 * <p>
	 * Entity 字段 → DTO 字段映射：
	 *   scheduleId → id
	 *   workDate   → scheduleDate
	 *   timePeriod → timeSlot
	 *   totalQuota → maxAppointments
	 *   remainQuota → 通过 totalQuota - remainQuota 计算 currentAppointments
	 *   registrationFee → registrationFee
	 *   status → status
	 *   source → source
	 *   doctorId → doctorId
	 *   deptId → departmentId
	 */
	@Override
	public ScheduleResponse toResponse(DoctorSchedule schedule) {
		ScheduleResponse resp = new ScheduleResponse();
		resp.setId(schedule.getScheduleId());
		resp.setScheduleDate(schedule.getWorkDate());
		resp.setTimeSlot(schedule.getTimePeriod());
		resp.setMaxAppointments(schedule.getTotalQuota());
		resp.setStatus(schedule.getStatus());
		resp.setRegistrationFee(schedule.getRegistrationFee());
		resp.setSource(schedule.getSource());

		// 已预约数量 = 总量 - 剩余量
		if (schedule.getTotalQuota() != null && schedule.getRemainQuota() != null) {
			resp.setCurrentAppointments(schedule.getTotalQuota() - schedule.getRemainQuota());
		} else if (schedule.getTotalQuota() != null) {
			resp.setCurrentAppointments(0);
		}

		DoctorInfo doctor = null;
		if (schedule.getDoctorId() != null) {
			Map<Long, DoctorInfo> doctorMap = doctorQueryService.findDoctorInfoByIds(
					Collections.singleton(schedule.getDoctorId()));
			doctor = doctorMap.get(schedule.getDoctorId());
		}
		if (doctor != null) {
			resp.setDoctorId(doctor.getDoctorId());
			resp.setDoctorName(doctor.getDoctorName());
			resp.setDoctorNo(doctor.getDoctorNo());
			resp.setDoctorType(doctor.getDoctorType());
			resp.setTitle(doctor.getTitle());
			resp.setSpecialty(doctor.getSpecialty());
			if (doctor.getDeptId() != null) {
				try {
					DepartmentResponse dept = departmentQueryService.getDepartmentDetail(doctor.getDeptId());
					resp.setDepartmentId(dept.getId());
					resp.setDepartmentName(dept.getName());
				} catch (BusinessException ignored) {
				}
			}
		}

		return resp;
	}

	/**
	 * 批量将排班列表转换为 DTO（集中查询关联数据，避免 N+1 查询）。
	 */
	private List<ScheduleResponse> schedulesToResponses(List<DoctorSchedule> schedules) {
		Set<Long> doctorIds = new HashSet<>();
		for (DoctorSchedule s : schedules) {
			if (s.getDoctorId() != null) doctorIds.add(s.getDoctorId());
		}

		Map<Long, DoctorInfo> doctorMap = doctorIds.isEmpty()
				? Collections.emptyMap()
				: doctorQueryService.findDoctorInfoByIds(doctorIds);

		Set<Long> deptIds = new HashSet<>();
		for (DoctorInfo info : doctorMap.values()) {
			if (info.getDeptId() != null) deptIds.add(info.getDeptId());
		}
		Map<Long, DepartmentResponse> deptMap = deptIds.isEmpty()
				? Collections.emptyMap()
				: departmentQueryService.findDepartmentResponsesByIds(deptIds);

		List<ScheduleResponse> result = new ArrayList<>(schedules.size());
		for (DoctorSchedule s : schedules) {
			ScheduleResponse resp = new ScheduleResponse();
			resp.setId(s.getScheduleId());
			resp.setScheduleDate(s.getWorkDate());
			resp.setTimeSlot(s.getTimePeriod());
			resp.setMaxAppointments(s.getTotalQuota());
			resp.setStatus(s.getStatus());
			resp.setRegistrationFee(s.getRegistrationFee());
			resp.setSource(s.getSource());

			if (s.getTotalQuota() != null && s.getRemainQuota() != null) {
				resp.setCurrentAppointments(s.getTotalQuota() - s.getRemainQuota());
			} else if (s.getTotalQuota() != null) {
				resp.setCurrentAppointments(0);
			}

			DoctorInfo doctor = doctorMap.get(s.getDoctorId());
			if (doctor != null) {
				resp.setDoctorId(doctor.getDoctorId());
				resp.setDoctorName(doctor.getDoctorName());
				resp.setDoctorNo(doctor.getDoctorNo());
				resp.setDoctorType(doctor.getDoctorType());
				resp.setTitle(doctor.getTitle());
				resp.setSpecialty(doctor.getSpecialty());
				DepartmentResponse dept = deptMap.get(doctor.getDeptId());
				if (dept != null) {
					resp.setDepartmentId(dept.getId());
					resp.setDepartmentName(dept.getName());
				}
			}
			result.add(resp);
		}
		return result;
	}

	@Override
	public List<ScheduleRegistrationResponse> listRegistrationsByScheduleId(Long scheduleId) {
		LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
		regWrapper.eq(Registration::getScheduleId, scheduleId)
				.orderByAsc(Registration::getRegistrationId);
		List<Registration> registrations = registrationMapper.selectList(regWrapper);

		if (registrations == null || registrations.isEmpty()) return Collections.emptyList();

		Set<Long> patientIds = new HashSet<>();
		for (Registration r : registrations) {
			if (r.getPatientId() != null) patientIds.add(r.getPatientId());
		}

		Map<Long, Long> patientUserMap = new HashMap<>();
		if (!patientIds.isEmpty()) {
			Map<Long, Map<String, Object>> rows = registrationMapper.getUserIdMapByPatientIds(new ArrayList<>(patientIds));
			for (Map.Entry<Long, Map<String, Object>> entry : rows.entrySet()) {
				Object uid = entry.getValue().get("user_id");
				if (uid != null) {
					patientUserMap.put(entry.getKey(), Long.valueOf(uid.toString()));
				}
			}
		}

		Set<Long> userIds = new HashSet<>(patientUserMap.values());
		Map<Long, UserInfo> userMap = userIds.isEmpty()
				? Collections.emptyMap()
				: userQueryService.findUsersByIds(userIds);

		List<ScheduleRegistrationResponse> result = new ArrayList<>(registrations.size());
		for (Registration registration : registrations) {
			ScheduleRegistrationResponse resp = new ScheduleRegistrationResponse();
			resp.setRegistrationId(registration.getRegistrationId());
			resp.setRegistrationNo(registration.getRegistrationNo());
			resp.setPatientId(registration.getPatientId());
			resp.setQueueNo(registration.getQueueNo());
			resp.setRegistrationFee(registration.getRegistrationFee());
			resp.setFeeStatus(registration.getFeeStatus());
			resp.setSource(registration.getSource());
			resp.setStatus(registration.getStatus());
			resp.setRegisteredAt(registration.getRegisteredAt());
			Long userId = patientUserMap.get(registration.getPatientId());
			if (userId != null) {
				UserInfo user = userMap.get(userId);
				if (user != null) resp.setPatientName(user.getRealName());
			}
			result.add(resp);
		}
		return result;
	}

	@Override
	public long countActiveSchedulesByDoctor(Long doctorId) {
		if (doctorId == null) return 0L;
		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DoctorSchedule::getDoctorId, doctorId)
				.ge(DoctorSchedule::getWorkDate, LocalDate.now())
				.in(DoctorSchedule::getStatus, "待接诊", "已开放");
		Long count = scheduleMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}

	@Override
	public long countPendingRegistrationsByDoctor(Long doctorId) {
		if (doctorId == null) return 0L;
		LambdaQueryWrapper<DoctorSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
		scheduleWrapper.eq(DoctorSchedule::getDoctorId, doctorId);
		List<DoctorSchedule> doctorSchedules = scheduleMapper.selectList(scheduleWrapper);
		if (doctorSchedules == null || doctorSchedules.isEmpty()) return 0L;
		Set<Long> scheduleIds = doctorSchedules.stream()
				.map(DoctorSchedule::getScheduleId)
				.collect(Collectors.toSet());

		LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
		regWrapper.in(Registration::getScheduleId, scheduleIds)
				.in(Registration::getStatus, "待接诊", "待就诊", "已预约");
		Long count = registrationMapper.selectCount(regWrapper);
		return count != null ? count : 0L;
	}

	@Override
	public long countSchedulesByDepartment(Long deptId) {
		if (deptId == null) return 0L;
		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DoctorSchedule::getDeptId, deptId);
		Long count = scheduleMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}

	@Override
	public long countDistinctDoctorsByDateAndStatuses(LocalDate date, Set<String> statuses) {
		if (date == null || statuses == null || statuses.isEmpty()) return 0L;
		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(DoctorSchedule::getWorkDate, date).in(DoctorSchedule::getStatus, statuses);
		List<DoctorSchedule> schedules = scheduleMapper.selectList(wrapper);
		if (schedules == null || schedules.isEmpty()) return 0L;
		Set<Long> distinctDoctorIds = new HashSet<>();
		for (DoctorSchedule s : schedules) {
			if (s.getDoctorId() != null) distinctDoctorIds.add(s.getDoctorId());
		}
		return distinctDoctorIds.size();
	}

	@Override
	public Map<Long, Long> countRegistrationsByScheduleIds(Set<Long> scheduleIds) {
		if (scheduleIds == null || scheduleIds.isEmpty()) {
			return Collections.emptyMap();
		}
		LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(Registration::getScheduleId, scheduleIds).select(Registration::getScheduleId);
		List<Registration> registrations = registrationMapper.selectList(wrapper);
		Map<Long, Long> result = new HashMap<>();
		if (registrations != null) {
			for (Registration r : registrations) {
				if (r.getScheduleId() != null) {
					result.merge(r.getScheduleId(), 1L, Long::sum);
				}
			}
		}
		return result;
	}

	@Override
	public long countRegistrationsByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
		LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
		wrapper.ge(Registration::getCreatedAt, startTime).lt(Registration::getCreatedAt, endTime);
		Long count = registrationMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}

	/** 自动将已过期的可预约/约满排班标记为"已过期"。 */
	void expireOutdatedSchedules() {
		LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(DoctorSchedule::getStatus, DoctorSchedule.STATUS_ACTIVE, DoctorSchedule.STATUS_FULL)
				.lt(DoctorSchedule::getWorkDate, LocalDate.now());
		List<DoctorSchedule> expired = scheduleMapper.selectList(wrapper);
		if (expired != null && !expired.isEmpty()) {
			for (DoctorSchedule s : expired) {
				s.setStatus(DoctorSchedule.STATUS_EXPIRED);
				s.setUpdatedAt(java.time.LocalDateTime.now());
				scheduleMapper.updateById(s);
			}
		}
	}
}
