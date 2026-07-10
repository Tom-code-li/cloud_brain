package com.hospital.ai.exam;

import com.hospital.ai.common.AbstractRoleAiAssistant;
import com.hospital.ai.common.RoleAiConfigRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamAiAssistant extends AbstractRoleAiAssistant {
    public ExamAiAssistant(RoleAiConfigRegistry configRegistry) {
        super(configRegistry, "EXAM", List.of(
                "REPORT_DRAFT",
                "REPORT_OPTIMIZE"
        ));
    }

    @Override
    protected String buildScenePrompt(String sceneCode, String context) {
        return switch (sceneCode) {
            case "REPORT_DRAFT" -> "检查医生检查报告生成：根据检查所见生成结构化报告草稿，报告必须由检查医生审核发布。上下文：" + context;
            case "REPORT_OPTIMIZE" -> "检查医生检查报告优化：优化报告描述、结论表达和医学术语一致性，不改变医生确认的事实结果。上下文：" + context;
            default -> "检查医生通用 AI 辅助：围绕检查报告生成和报告内容优化提供参考。上下文：" + context;
        };
    }
}
