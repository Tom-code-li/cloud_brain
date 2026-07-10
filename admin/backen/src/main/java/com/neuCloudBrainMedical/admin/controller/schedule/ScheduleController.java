package com.neuCloudBrainMedical.admin.controller.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleRegistrationResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 排班资源接口。
 * 资源路径：/api/admin/schedules
 */
@RestController
@RequestMapping("/api/admin/schedules")
public class ScheduleController {

	private final IScheduleQueryService scheduleQueryService;
	private final IScheduleCommandService scheduleCommandService;

	public ScheduleController(IScheduleQueryService scheduleQueryService,
			IScheduleCommandService scheduleCommandService) {
		this.scheduleQueryService = scheduleQueryService;
		this.scheduleCommandService = scheduleCommandService;
	}

	// ========== 查询 ==========

	@GetMapping
	public Result<List<ScheduleResponse>> listSchedules(
			@RequestParam(required = false) Long departmentId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return Result.success(scheduleQueryService.listSchedules(departmentId, startDate, endDate));
	}

	@GetMapping("/{id}")
	public Result<ScheduleResponse> getScheduleDetail(@PathVariable Long id) {
		return Result.success(scheduleQueryService.getScheduleDetail(id));
	}

	@GetMapping("/{id}/registrations")
	public Result<List<ScheduleRegistrationResponse>> listRegistrations(@PathVariable Long id) {
		return Result.success(scheduleQueryService.listRegistrationsByScheduleId(id));
	}

	// ========== 命令 ==========

	@PostMapping
	public Result<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
		return Result.success(scheduleCommandService.createSchedule(request));
	}

	@PostMapping("/batch")
	public Result<List<ScheduleResponse>> batchCreateSchedule(@Valid @RequestBody ScheduleBatchCreateRequest request) {
		return Result.success(scheduleCommandService.batchCreateSchedule(request));
	}

	@PutMapping("/{id}")
	public Result<ScheduleResponse> updateSchedule(@PathVariable Long id, @RequestBody ScheduleUpdateRequest request) {
		return Result.success(scheduleCommandService.updateSchedule(id, request));
	}

	@DeleteMapping("/{id}")
	public Result<Void> cancelSchedule(@PathVariable Long id) {
		scheduleCommandService.cancelSchedule(id);
		return Result.success();
	}
}