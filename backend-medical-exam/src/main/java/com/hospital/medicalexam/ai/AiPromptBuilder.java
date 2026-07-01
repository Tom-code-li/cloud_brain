package com.hospital.medicalexam.ai;

import org.springframework.stereotype.Component;

@Component
public class AiPromptBuilder {
    public String build(AiDraftRequest request) {
        return """
                你是医院检查检验报告辅助系统。请根据医生输入的检查/检验结果生成结构化辅助草稿。

                要求：
                1. 不要替代医生最终诊断。
                2. 输出必须谨慎，不能夸大病情。
                3. 重点给出报告草稿、可能方向、风险级别、异常摘要、复核建议。
                4. 风险级别只能是 LOW、MEDIUM、HIGH、UNKNOWN。

                患者姓名：%s
                性别：%s
                项目类型：%s
                项目名称：%s
                医生输入结果：%s

                请输出 JSON：
                {
                  "reportDraft": "...",
                  "possibleDirection": "...",
                  "riskLevel": "LOW|MEDIUM|HIGH|UNKNOWN",
                  "abnormalSummary": ["..."],
                  "reviewSuggestions": ["..."]
                }
                """.formatted(
                safe(request.patientName()),
                safe(request.gender()),
                safe(request.reportType()),
                safe(request.itemName()),
                safe(request.resultDetail())
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未提供" : value.trim();
    }
}
