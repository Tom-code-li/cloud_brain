package com.neuCloudBrainMedical.admin.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SysRoleRequest {

	@NotBlank(message = "角色编码不能为空")
	@Size(max = 50, message = "角色编码长度不能超过50")
	private String roleCode;

	@NotBlank(message = "角色名称不能为空")
	@Size(max = 50, message = "角色名称长度不能超过50")
	private String roleName;

	@Size(max = 255, message = "描述长度不能超过255")
	private String description;

	private Integer status = 1;

	public String getRoleCode() { return roleCode; }
	public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
}