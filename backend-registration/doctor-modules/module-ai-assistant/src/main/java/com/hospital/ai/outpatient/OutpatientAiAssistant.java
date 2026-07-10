package com.hospital.ai.outpatient;

import com.hospital.ai.common.AbstractRoleAiAssistant;
import com.hospital.ai.common.RoleAiConfigRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutpatientAiAssistant extends AbstractRoleAiAssistant {
    public OutpatientAiAssistant(RoleAiConfigRegistry configRegistry) {
        super(configRegistry, "OUTPATIENT", List.of(
                "PATIENT_SUMMARY",
                "DIAGNOSIS",
                "EXAM_SUGGESTION",
                "REPORT_INTERPRET",
                "PRESCRIPTION",
                "MEDICAL_RECORD_GENERATE"
        ));
    }

    @Override
    protected String buildScenePrompt(String sceneCode, String context) {
        return switch (sceneCode) {
            case "PATIENT_SUMMARY" -> "门诊医生患者摘要：提炼患者基础信息、过敏史、主诉和既往记录，辅助接诊前快速了解情况。上下文：" + context;
            case "DIAGNOSIS" -> "门诊医生辅助诊断建议：结合主诉、现病史、体格检查和报告结果，给出鉴别诊断方向与风险提醒。上下文：" + context;
            case "EXAM_SUGGESTION" -> "门诊医生检查/检验建议：根据初诊病历和临床问题推荐检查、检验项目，开立前必须由门诊医生确认。上下文：" + context;
            case "REPORT_INTERPRET" -> "门诊医生报告解读：解释检查/检验报告中的异常结果、可能意义和下一步处理建议。上下文：" + context;
            case "PRESCRIPTION" -> "门诊医生处方建议：结合诊断、过敏史、禁忌证、剂量和用法生成处方参考。上下文：" + context;
            case "MEDICAL_RECORD_GENERATE" -> "门诊医生病历生成：按主诉、现病史、体格检查、诊断、处理意见补全电子病历。上下文：" + context;
            default -> "门诊医生通用 AI 辅助：围绕接诊、诊断、报告解读、处方和病历生成提供参考。上下文：" + context;
        };
    }
}
