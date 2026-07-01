package com.hospital.medicalexam.ai;

import java.util.List;

public record AiDraftResult(
        String reportDraft,
        String possibleDirection,
        String riskLevel,
        List<String> abnormalSummary,
        List<String> reviewSuggestions
) {
}
