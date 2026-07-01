package com.doctor.platform.ai.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDraftBlock {

    private String sceneCode;
    private String backgroundSummary;
    private String diagnosisDraft;
    private String planDraft;
    private List<String> evidence;
    private List<String> examSuggestions;
    private List<PossibleDiagnosisItem> possibleDiagnoses;
    private List<ExamRecommendationItem> examRecommendations;
    private List<String> drugSuggestions;
    private List<String> riskFlags;
    private String reportSummary;
}
