package com.neu.patient.dto;

import java.util.List;

public class AiConsultResponse {
    private String mode;
    private String summary;
    private String riskLevel;
    private String recommendedDept;
    private String recommendedDoctor;
    private List<String> candidates;
    private String aiResult;
    private String note;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendedDept() { return recommendedDept; }
    public void setRecommendedDept(String recommendedDept) { this.recommendedDept = recommendedDept; }
    public String getRecommendedDoctor() { return recommendedDoctor; }
    public void setRecommendedDoctor(String recommendedDoctor) { this.recommendedDoctor = recommendedDoctor; }
    public List<String> getCandidates() { return candidates; }
    public void setCandidates(List<String> candidates) { this.candidates = candidates; }
    public String getAiResult() { return aiResult; }
    public void setAiResult(String aiResult) { this.aiResult = aiResult; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
