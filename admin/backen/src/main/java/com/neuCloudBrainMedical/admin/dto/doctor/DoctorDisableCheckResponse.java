package com.neuCloudBrainMedical.admin.dto.doctor;

/**
 * 医生禁用前的关联数据检查结果。
 * 前端用于在禁用前提示管理员。
 */
public class DoctorDisableCheckResponse {

	private long pendingSchedules;
	private long pendingRegistrations;

	public DoctorDisableCheckResponse() {}

	public DoctorDisableCheckResponse(long pendingSchedules, long pendingRegistrations) {
		this.pendingSchedules = pendingSchedules;
		this.pendingRegistrations = pendingRegistrations;
	}

	public boolean hasPending() {
		return pendingSchedules > 0 || pendingRegistrations > 0;
	}

	public long getPendingSchedules() { return pendingSchedules; }
	public void setPendingSchedules(long pendingSchedules) { this.pendingSchedules = pendingSchedules; }

	public long getPendingRegistrations() { return pendingRegistrations; }
	public void setPendingRegistrations(long pendingRegistrations) { this.pendingRegistrations = pendingRegistrations; }
}



