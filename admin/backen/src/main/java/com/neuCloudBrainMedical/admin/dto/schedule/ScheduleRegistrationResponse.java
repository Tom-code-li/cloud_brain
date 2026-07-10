package com.neuCloudBrainMedical.admin.dto.schedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 排班下的挂号记录 DTO。
 * 封装单个挂号的展示所需字段，避免将实体直接暴露给前端。
 */
public class ScheduleRegistrationResponse {

	private Long registrationId;
	private String registrationNo;
	private Long patientId;
	private String patientName;
	private Integer queueNo;
	private BigDecimal registrationFee;
	private String feeStatus;
	private String status;
	private String source;
	private LocalDateTime registeredAt;

	public Long getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(Long registrationId) {
		this.registrationId = registrationId;
	}

	public String getRegistrationNo() {
		return registrationNo;
	}

	public void setRegistrationNo(String registrationNo) {
		this.registrationNo = registrationNo;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public Integer getQueueNo() {
		return queueNo;
	}

	public void setQueueNo(Integer queueNo) {
		this.queueNo = queueNo;
	}

	public BigDecimal getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(BigDecimal registrationFee) {
		this.registrationFee = registrationFee;
	}

	public String getFeeStatus() {
		return feeStatus;
	}

	public void setFeeStatus(String feeStatus) {
		this.feeStatus = feeStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public LocalDateTime getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(LocalDateTime registeredAt) {
		this.registeredAt = registeredAt;
	}
}