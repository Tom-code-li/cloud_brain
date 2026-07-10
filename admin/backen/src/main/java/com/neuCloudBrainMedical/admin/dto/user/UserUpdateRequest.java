package com.neuCloudBrainMedical.admin.dto.user;

/**
 * 用户更新请求（跨模块用户管理使用）。
 *
 * <p>字段为 null 表示不更新。userId 必传。</p>
 */
public class UserUpdateRequest {

	private Long userId;
	private Long roleId;
	private String username;
	private String password;
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
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getRealName() { return realName; }
	public void setRealName(String realName) { this.realName = realName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
}