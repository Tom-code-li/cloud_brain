package com.neu.patient.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

@TableName("ai_consultation")
public class AiConsultation {
    @TableId
    private Long consultationId;
    private Long patientId;
    private String chiefComplaint;
    private String symptomDetail;
    private String aiSummary;
    private Long recommendedDeptId;
    private String riskLevel;
    private String aiResult;
    private String status;
    private LocalDateTime createdAt;
    @TableField(exist = false)
    private String recommendedDeptName;

    public Long getConsultationId() { return consultationId; }
    public void setConsultationId(Long consultationId) { this.consultationId = consultationId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getSymptomDetail() { return symptomDetail; }
    public void setSymptomDetail(String symptomDetail) { this.symptomDetail = symptomDetail; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public Long getRecommendedDeptId() { return recommendedDeptId; }
    public void setRecommendedDeptId(Long recommendedDeptId) { this.recommendedDeptId = recommendedDeptId; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getAiResult() { return aiResult; }
    public void setAiResult(String aiResult) { this.aiResult = aiResult; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getRecommendedDeptName() { return recommendedDeptName; }
    public void setRecommendedDeptName(String recommendedDeptName) { this.recommendedDeptName = recommendedDeptName; }
}
