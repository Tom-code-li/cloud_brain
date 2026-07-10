package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.SuggestionDetailResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 排班建议查询服务（读操作语义）。
 *
 * <p>边界规则：仅依赖自己模块的 Mapper（AiScheduleSuggestionMapper、AiScheduleSuggestionDetailMapper），
 * 不跨模块访问其他模块的 Mapper。</p>
 */
public interface IAISuggestionQueryService {

	/** 统计指定时间范围内的 AI 排班建议生成次数。 */
	long countSuggestionsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

	/** 查询所有 AI 排班建议列表（按创建时间倒序）。 */
	List<AIScheduleSuggestionResponse> listSuggestions();

	/** 查询指定科室下的 AI 排班建议列表（按创建时间倒序）。 */
	List<AIScheduleSuggestionResponse> listSuggestionsByDepartment(Long departmentId);

	/** 查询指定 AI 排班建议详情（含明细列表）。 */
	AIScheduleSuggestionResponse getSuggestionDetail(Long suggestionId);

	/** 查询指定建议下的所有明细（不含建议本身）。 */
	List<SuggestionDetailResponse> listDetailsBySuggestionId(Long suggestionId);

	/** 将建议 Entity 转换为 Response DTO（供本模块 CommandService 复用）。 */
	AIScheduleSuggestionResponse toResponse(com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion suggestion,
			List<com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail> details);
}