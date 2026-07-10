package com.neuCloudBrainMedical.admin.mapper.admin.schedule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;

/**
 * AI 排班建议 Mapper（排班模块内部使用）。
 *
 * <p>跨模块访问请通过 {@link com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionQueryService}。</p>
 */
public interface AiScheduleSuggestionMapper extends BaseMapper<AiScheduleSuggestion> {
}
