package com.hospital.medicalexam.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "simulated", matchIfMissing = true)
public class FallbackAiDraftService implements AiDraftService {
    @Override
    public String generate(AiDraftRequest request) {
        return """
                AI辅助草稿：
                项目：%s
                类型：%s
                患者：%s（%s）
                结果摘要：%s
                可能方向：请结合患者症状、体征及既往史综合判断。
                风险提示：AI内容仅作辅助参考，必须由医生确认后发布。
                复核建议：医生确认报告描述、结论和临床一致性后再提交。
                """.formatted(
                safe(request.itemName()),
                safe(request.reportType()),
                safe(request.patientName()),
                safe(request.gender()),
                safe(request.resultDetail())
        ).trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "未提供" : value.trim();
    }
}
