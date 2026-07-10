package com.neu.patient.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("triage_record")
public class TriageRecord {
    @TableId
    private Long triageId;
    private Long patientId;
    private Long consultationId;
    private Long registrationId;
    private Long triageDoctorId;
    private Long recommendedDeptId;
    private String chiefComplaint;
    private String riskLevel;
    private String triageResult;
    private String status;
    private LocalDateTime createdAt;

    public Long getTriageId() { return triageId; }
    public void setTriageId(Long triageId) { this.triageId = triageId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getConsultationId() { return consultationId; }
    public void setConsultationId(Long consultationId) { this.consultationId = consultationId; }
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public Long getTriageDoctorId() { return triageDoctorId; }
    public void setTriageDoctorId(Long triageDoctorId) { this.triageDoctorId = triageDoctorId; }
    public Long getRecommendedDeptId() { return recommendedDeptId; }
    public void setRecommendedDeptId(Long recommendedDeptId) { this.recommendedDeptId = recommendedDeptId; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getTriageResult() { return triageResult; }
    public void setTriageResult(String triageResult) { this.triageResult = triageResult; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
