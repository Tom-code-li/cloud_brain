package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleRegistrationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 排班查询服务（读操作）。
 * 单一职责：负责排班列表、详情、以及排班下挂号记录的查询与组装。
 */
public interface IScheduleQueryService {

	List<ScheduleResponse> listSchedules(Long departmentId, LocalDate startDate, LocalDate endDate);

	ScheduleResponse getScheduleDetail(Long id);

	/** 将单个 DoctorSchedule 实体转换为 DTO（含医生、科室、用户信息查询）。 */
	ScheduleResponse toResponse(com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule schedule);

	/** 查询指定排班下的所有挂号记录（含病人姓名）。 */
	List<ScheduleRegistrationResponse> listRegistrationsByScheduleId(Long scheduleId);

	/** 统计指定医生未来的有效排班数量。 */
	long countActiveSchedulesByDoctor(Long doctorId);

	/** 统计指定医生的待处理挂号数量（待接诊/待就诊/已预约）。 */
	long countPendingRegistrationsByDoctor(Long doctorId);

	/** 统计指定科室下的排班数量（用于删除科室前检查）。 */
	long countSchedulesByDepartment(Long deptId);

	/** 统计指定日期下、排班状态匹配的医生数量（用于仪表盘统计）。 */
	long countDistinctDoctorsByDateAndStatuses(LocalDate date, Set<String> statuses);

	/** 批量统计每个排班的挂号数量（返回 scheduleId -> 挂号计数 的 Map）。 */
	Map<Long, Long> countRegistrationsByScheduleIds(Set<Long> scheduleIds);

	/** 统计指定时间范围内的挂号数量（用于仪表盘统计今日挂号）。 */
	long countRegistrationsByTimeRange(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}