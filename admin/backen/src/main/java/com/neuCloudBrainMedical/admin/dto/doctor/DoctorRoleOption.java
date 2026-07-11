package com.neuCloudBrainMedical.admin.dto.doctor;

/**
 * 医生可选的权限角色信息。
 * 不同角色的医生登录后会进入不同的页面。
 */
public class DoctorRoleOption {

	private Long roleId;
	private String roleCode;
	private String roleName;
	private String description;

	public DoctorRoleOption() {
	}

	public DoctorRoleOption(Long roleId, String roleCode, String roleName, String description) {
		this.roleId = roleId;
		this.roleCode = roleCode;
		this.roleName = roleName;
		this.description = description;
	}

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }

	public String getRoleCode() { return roleCode; }
	public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}