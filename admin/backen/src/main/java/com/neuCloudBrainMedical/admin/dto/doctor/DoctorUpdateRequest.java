package com.neuCloudBrainMedical.admin.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 更新医生请求。
 * 所有字段均可选；doctorNo 不可修改（由 Service 层控制）。
 * 登录账号/密码/权限角色 字段用于变更 sys_user 中的账号信息。
 */
public class DoctorUpdateRequest {

	@Size(max = 50, message = "医生姓名长度不能超过50")
	private String name;

	@Size(max = 50, message = "登录账号长度不能超过50")
	private String loginUsername;

	@Size(max = 50, message = "密码长度不能超过50")
	private String loginPassword;

	private Long roleId;

	@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;

	@Email(message = "邮箱格式不正确")
	@Size(max = 100, message = "邮箱长度不能超过100")
	private String email;

	private Long departmentId;

	@Pattern(regexp = "^(住院医师|主治医师|副主任医师|主任医师)$", message = "职称只能是住院医师、主治医师、副主任医师、主任医师")
	private String title;

	@Size(max = 30, message = "医生类型长度不能超过30")
	private String doctorType;

	@Size(max = 255, message = "专长长度不能超过255")
	private String specialty;

	private LocalDate hireDate;

	@Size(max = 500, message = "简介长度不能超过500")
	private String introduction;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getLoginUsername() { return loginUsername; }
	public void setLoginUsername(String loginUsername) { this.loginUsername = loginUsername; }

	public String getLoginPassword() { return loginPassword; }
	public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public Long getDepartmentId() { return departmentId; }
	public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDoctorType() { return doctorType; }
	public void setDoctorType(String doctorType) { this.doctorType = doctorType; }

	public String getSpecialty() { return specialty; }
	public void setSpecialty(String specialty) { this.specialty = specialty; }

	public LocalDate getHireDate() { return hireDate; }
	public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

	public String getIntroduction() { return introduction; }
	public void setIntroduction(String introduction) { this.introduction = introduction; }
}