package com.neuCloudBrainMedical.admin.dto.doctor;

/**
 * 医生信息 DTO（Service 间传递）。
 *
 * <p>用于跨 Service 传递医生关联信息，避免直接暴露 Doctor Entity。
 * 包含组装 ScheduleResponse、AI 排班建议所需的所有必要字段。</p>
 */
public class DoctorInfo {

	private Long doctorId;
	private String doctorNo;
	private String doctorType;
	private String doctorName;
	private String title;
	private String specialty;
	private Long deptId;
	private Long userId;
	private Long historicalWorkDays;

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getDoctorNo() {
		return doctorNo;
	}

	public void setDoctorNo(String doctorNo) {
		this.doctorNo = doctorNo;
	}

	public String getDoctorType() {
		return doctorType;
	}

	public void setDoctorType(String doctorType) {
		this.doctorType = doctorType;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getHistoricalWorkDays() {
		return historicalWorkDays;
	}

	public void setHistoricalWorkDays(Long historicalWorkDays) {
		this.historicalWorkDays = historicalWorkDays;
	}
}