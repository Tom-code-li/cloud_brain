package com.neuCloudBrainMedical.admin.dto.doctor;

import java.time.LocalDate;

/**
 * 医生信息响应。
 * 聚合 doctor 与 sys_user 两个实体的数据，隐藏敏感字段（如 password）。
 */
public class DoctorResponse {

	private Long doctorId;
	private String doctorNo;
	private String doctorName;
	private String phone;
	private String email;

	/** 登录账号（sys_user.username） */
	private String loginUsername;

	/** 关联角色 ID */
	private Long roleId;
	/** 角色编码（如 DOCTOR_CLINIC / DOCTOR_INPATIENT / DOCTOR_CHIEF） */
	private String roleCode;
	/** 角色名称（如 门诊医生 / 住院医生 / 主任医师） */
	private String roleName;

	private Long departmentId;
	private String departmentName;

	private String doctorType;
	private String title;
	private String specialty;

	private Integer status;
	private String statusText;

	private LocalDate hireDate;
	private String introduction;

	private String createdAt;
	private String updatedAt;

	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

	public String getDoctorNo() { return doctorNo; }
	public void setDoctorNo(String doctorNo) { this.doctorNo = doctorNo; }

	public String getDoctorName() { return doctorName; }
	public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getLoginUsername() { return loginUsername; }
	public void setLoginUsername(String loginUsername) { this.loginUsername = loginUsername; }

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }

	public String getRoleCode() { return roleCode; }
	public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }

	public Long getDepartmentId() { return departmentId; }
	public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

	public String getDepartmentName() { return departmentName; }
	public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

	public String getDoctorType() { return doctorType; }
	public void setDoctorType(String doctorType) { this.doctorType = doctorType; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getSpecialty() { return specialty; }
	public void setSpecialty(String specialty) { this.specialty = specialty; }

	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }

	public String getStatusText() { return statusText; }
	public void setStatusText(String statusText) { this.statusText = statusText; }

	public LocalDate getHireDate() { return hireDate; }
	public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

	public String getIntroduction() { return introduction; }
	public void setIntroduction(String introduction) { this.introduction = introduction; }

	public String getCreatedAt() { return createdAt; }
	public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

	public String getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}