package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;

import java.util.List;

/**
 * AI 排班建议命令服务（写操作语义）。
 *
 * 所有方法都会修改数据库状态（生成建议会写入建议和明细，采纳/拒绝会修改状态并可能创建排班）。
 */
public interface IAISuggestionCommandService {

	/** 生成 AI 排班建议（写入数据库）。 */
	AIScheduleSuggestionResponse generateSuggestion(AIScheduleSuggestRequest request);

	/** 采纳整份建议（所有未处理明细）。 */
	List<ScheduleResponse> acceptSuggestion(Long suggestionId);

	/** 拒绝整份建议（所有未处理明细）。 */
	void rejectSuggestion(Long suggestionId);

	/** 采纳指定明细。 */
	ScheduleResponse acceptSuggestionDetail(Long suggestionId, Long detailId);

	/** 拒绝指定明细。 */
	void rejectSuggestionDetail(Long suggestionId, Long detailId);
}