package com.hospital.ai.lab;

import com.hospital.ai.common.AbstractRoleAiAssistant;
import com.hospital.ai.common.RoleAiConfigRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LabAiAssistant extends AbstractRoleAiAssistant {
    public LabAiAssistant(RoleAiConfigRegistry configRegistry) {
        super(configRegistry, "LAB", List.of(
                "RESULT_INTERPRET",
                "REPORT_CONCLUSION"
        ));
    }

    @Override
    protected String buildScenePrompt(String sceneCode, String context) {
        return switch (sceneCode) {
            case "RESULT_INTERPRET" -> "检验医生检验结果解读：分析检验指标、参考范围和异常提示，辅助检验医生判断。上下文：" + context;
            case "REPORT_CONCLUSION" -> "检验医生报告结论生成：根据已确认的检验指标生成报告结论草稿，发布前必须由检验医生确认。上下文：" + context;
            default -> "检验医生通用 AI 辅助：围绕检验结果解读和报告结论生成提供参考。上下文：" + context;
        };
    }
}
