package com.neuCloudBrainMedical.admin.dto.department;

public class DepartmentOverviewBrief {

	private String departmentName;
	private Long doctorCount;

	public DepartmentOverviewBrief() {
	}

	public DepartmentOverviewBrief(String departmentName, Long doctorCount) {
		this.departmentName = departmentName;
		this.doctorCount = doctorCount;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Long getDoctorCount() {
		return doctorCount;
	}

	public void setDoctorCount(Long doctorCount) {
		this.doctorCount = doctorCount;
	}
}




