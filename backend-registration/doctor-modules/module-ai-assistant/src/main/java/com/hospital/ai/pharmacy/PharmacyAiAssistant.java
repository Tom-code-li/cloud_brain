package com.hospital.ai.pharmacy;

import com.hospital.ai.common.AbstractRoleAiAssistant;
import com.hospital.ai.common.RoleAiConfigRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PharmacyAiAssistant extends AbstractRoleAiAssistant {
    public PharmacyAiAssistant(RoleAiConfigRegistry configRegistry) {
        super(configRegistry, "PHARMACY", List.of(
                "PRESCRIPTION_SUMMARY",
                "MEDICATION_REVIEW"
        ));
    }

    @Override
    protected String buildScenePrompt(String sceneCode, String context) {
        return switch (sceneCode) {
            case "PRESCRIPTION_SUMMARY" -> "药房医生处方摘要：提炼诊断、药品、用法用量、费用状态和发药状态，辅助药师快速审核。上下文：" + context;
            case "MEDICATION_REVIEW" -> "药房医生用药提醒：核对过敏史、相互作用、重复用药、禁忌证和剂量范围，审核结论由药房医生确认。上下文：" + context;
            default -> "药房医生通用 AI 辅助：围绕处方摘要和用药提醒提供参考。上下文：" + context;
        };
    }
}
