package com.neuCloudBrainMedical.admin.dto.user;

/**
 * 用户信息 DTO（Service 间传递）。
 *
 * <p>用于跨 Service 传递用户关联信息，避免直接暴露 SysUser Entity。
 * 不包含敏感字段（密码、最后登录时间、创建/更新时间）。</p>
 */
public class UserInfo {

	private Long userId;
	private Long roleId;
	private String username;
	private String realName;
	private String phone;
	private String email;
	private Integer status;

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getRoleId() { return roleId; }
	public void setRoleId(Long roleId) { this.roleId = roleId; }
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getRealName() { return realName; }
	public void setRealName(String realName) { this.realName = realName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
}