package com.neuCloudBrainMedical.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sys_user")
public class SysUser {

	@TableId(value = "user_id", type = IdType.AUTO)
	private Long userId;
	private Long roleId;
	private String username;
	private String password;
	private String realName;
	private String phone;
	private String email;
	private Integer status;
	private LocalDateTime lastLoginAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

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
	public LocalDateTime getLastLoginAt() { return lastLoginAt; }
	public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}