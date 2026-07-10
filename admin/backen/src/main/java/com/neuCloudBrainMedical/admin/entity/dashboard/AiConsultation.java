package com.neuCloudBrainMedical.admin.entity.dashboard;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_consultation")
public class AiConsultation {
	@TableId(value = "consultation_id", type = IdType.AUTO)
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

	public Long getConsultationId() { return consultationId; }
	public void setConsultationId(Long v) { this.consultationId = v; }
	public Long getPatientId() { return patientId; }
	public void setPatientId(Long v) { this.patientId = v; }
	public String getChiefComplaint() { return chiefComplaint; }
	public void setChiefComplaint(String v) { this.chiefComplaint = v; }
	public String getSymptomDetail() { return symptomDetail; }
	public void setSymptomDetail(String v) { this.symptomDetail = v; }
	public String getAiSummary() { return aiSummary; }
	public void setAiSummary(String v) { this.aiSummary = v; }
	public Long getRecommendedDeptId() { return recommendedDeptId; }
	public void setRecommendedDeptId(Long v) { this.recommendedDeptId = v; }
	public String getRiskLevel() { return riskLevel; }
	public void setRiskLevel(String v) { this.riskLevel = v; }
	public String getAiResult() { return aiResult; }
	public void setAiResult(String v) { this.aiResult = v; }
	public String getStatus() { return status; }
	public void setStatus(String v) { this.status = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}