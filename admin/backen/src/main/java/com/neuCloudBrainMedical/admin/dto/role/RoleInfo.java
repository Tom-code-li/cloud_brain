package com.neuCloudBrainMedical.admin.dto.role;

/**
 * 角色信息 DTO（Service 间传递）。
 *
 * <p>用于跨 Service 传递角色关联信息，避免直接暴露 SysRole Entity。
 * 不包含状态、创建/更新时间等字段。</p>
 */
public class RoleInfo {

	private Long roleId;
	private String roleCode;
	private String roleName;
	private String description;

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
	private Integer status;
}
