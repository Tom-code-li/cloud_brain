package com.neuCloudBrainMedical.admin.dto.role;

public class SysRoleResponse {
	private Long roleId;
	private String roleCode;
	private String roleName;
	private String description;
	private Integer status;

	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }
	public String getRoleCode() { return roleCode; }
	public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
}