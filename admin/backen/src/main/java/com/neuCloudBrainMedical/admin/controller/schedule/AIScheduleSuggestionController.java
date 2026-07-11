package com.neuCloudBrainMedical.admin.controller.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionCommandService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 排班建议 Controller：路由前缀 /api/admin/schedules/ai-suggestions。
 * 只保留前端实际调用的写操作（生成/采纳/拒绝）。
 */
@RestController
@RequestMapping("/api/admin/schedules/ai-suggestions")
public class AIScheduleSuggestionController {

	private final IAISuggestionCommandService service;

	public AIScheduleSuggestionController(IAISuggestionCommandService service) {
		this.service = service;
	}

	@PostMapping
	public Result<AIScheduleSuggestionResponse> generateSuggestion(@Valid @RequestBody AIScheduleSuggestRequest request) {
		return Result.success(service.generateSuggestion(request));
	}

	@PostMapping("/{suggestionId}/accept")
	public Result<List<ScheduleResponse>> acceptSuggestion(@PathVariable Long suggestionId) {
		return Result.success(service.acceptSuggestion(suggestionId));
	}

	@PostMapping("/{suggestionId}/reject")
	public Result<Void> rejectSuggestion(@PathVariable Long suggestionId) {
		service.rejectSuggestion(suggestionId);
		return Result.success();
	}

	@PostMapping("/{suggestionId}/details/{detailId}/accept")
	public Result<ScheduleResponse> acceptSuggestionDetail(@PathVariable Long suggestionId, @PathVariable Long detailId) {
		return Result.success(service.acceptSuggestionDetail(suggestionId, detailId));
	}

	@PostMapping("/{suggestionId}/details/{detailId}/reject")
	public Result<Void> rejectSuggestionDetail(@PathVariable Long suggestionId, @PathVariable Long detailId) {
		service.rejectSuggestionDetail(suggestionId, detailId);
		return Result.success();
	}
}