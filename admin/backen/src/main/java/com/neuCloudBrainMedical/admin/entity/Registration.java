package com.neuCloudBrainMedical.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("registration")
public class Registration {
	@TableId(value = "registration_id", type = IdType.AUTO)
	private Long registrationId;
	private Long patientId;
	private Long consultationId;
	private Long deptId;
	private Long doctorId;
	private Long scheduleId;
	private Long operatorUserId;
	private String source;
	private String registrationNo;
	private Integer queueNo;
	private BigDecimal registrationFee;
	private String feeStatus;
	private String status;
	private LocalDateTime registeredAt;
	private LocalDateTime calledAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Long getRegistrationId() { return registrationId; }
	public void setRegistrationId(Long v) { this.registrationId = v; }
	public Long getPatientId() { return patientId; }
	public void setPatientId(Long v) { this.patientId = v; }
	public Long getConsultationId() { return consultationId; }
	public void setConsultationId(Long v) { this.consultationId = v; }
	public Long getDeptId() { return deptId; }
	public void setDeptId(Long v) { this.deptId = v; }
	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long v) { this.doctorId = v; }
	public Long getScheduleId() { return scheduleId; }
	public void setScheduleId(Long v) { this.scheduleId = v; }
	public Long getOperatorUserId() { return operatorUserId; }
	public void setOperatorUserId(Long v) { this.operatorUserId = v; }
	public String getSource() { return source; }
	public void setSource(String v) { this.source = v; }
	public String getRegistrationNo() { return registrationNo; }
	public void setRegistrationNo(String v) { this.registrationNo = v; }
	public Integer getQueueNo() { return queueNo; }
	public void setQueueNo(Integer v) { this.queueNo = v; }
	public BigDecimal getRegistrationFee() { return registrationFee; }
	public void setRegistrationFee(BigDecimal v) { this.registrationFee = v; }
	public String getFeeStatus() { return feeStatus; }
	public void setFeeStatus(String v) { this.feeStatus = v; }
	public String getStatus() { return status; }
	public void setStatus(String v) { this.status = v; }
	public LocalDateTime getRegisteredAt() { return registeredAt; }
	public void setRegisteredAt(LocalDateTime v) { this.registeredAt = v; }
	public LocalDateTime getCalledAt() { return calledAt; }
	public void setCalledAt(LocalDateTime v) { this.calledAt = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}