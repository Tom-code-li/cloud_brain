package com.neuCloudBrainMedical.admin.dto.user;

/**
 * 用户创建请求（跨模块用户管理使用）。
 *
 * <p>由 DoctorCommandServiceImpl 等外部 Service 调用 IUserCommandService 时使用，
 * 替代直接传递 SysUser Entity。</p>
 */
public class UserCreateRequest {

	private Long roleId;
	private String username;
	private String password;
	private String realName;
	private String phone;
	private String email;
	private Integer status;

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