package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;

import java.util.List;

/**
 * 排班命令服务（写操作语义）。
 *
 * <p>包含冲突校验、排班创建、批量创建、更新、取消等操作。
 * 其他 Service（如 AI 排班建议）应通过本接口创建实际排班，
 * 以复用冲突校验和标准化创建逻辑，避免代码重复。</p>
 */
public interface IScheduleCommandService {

	ScheduleResponse createSchedule(ScheduleCreateRequest request);

	List<ScheduleResponse> batchCreateSchedule(ScheduleBatchCreateRequest request);

	ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request);

	void cancelSchedule(Long id);

	/**
	 * 以指定来源创建排班（供内部 Service 调用，如 AI 排班建议）。
	 *
	 * @param request 排班创建参数
	 * @param source  排班来源标识（如 "AI_SUGGESTED" / "MANUAL"）
	 */
	ScheduleResponse createScheduleWithSource(ScheduleCreateRequest request, String source);
}