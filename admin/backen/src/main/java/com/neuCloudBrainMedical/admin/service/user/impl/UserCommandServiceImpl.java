package com.neuCloudBrainMedical.admin.service.user.impl;

import com.neuCloudBrainMedical.admin.dto.user.UserCreateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.service.user.IUserCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserCommandServiceImpl implements IUserCommandService {

	private static final String NOOP_PREFIX = "{noop}";

	private final SysUserMapper sysUserMapper;

	public UserCommandServiceImpl(SysUserMapper sysUserMapper) {
		this.sysUserMapper = sysUserMapper;
	}

	/**
	 * 将原始密码加上 {noop} 前缀后存储，与 seed.sql 预置数据格式保持一致。
	 * 如果已经带 {noop} 前缀，则不重复添加。
	 */
	private String encodePassword(String rawPassword) {
		if (rawPassword == null) return null;
		return rawPassword.startsWith(NOOP_PREFIX) ? rawPassword : NOOP_PREFIX + rawPassword;
	}

	@Override
	public UserInfo createUser(UserCreateRequest request) {
		LocalDateTime now = LocalDateTime.now();
		SysUser user = new SysUser();
		user.setRoleId(request.getRoleId());
		user.setUsername(request.getUsername());
		user.setPassword(encodePassword(request.getPassword()));
		user.setRealName(request.getRealName());
		user.setPhone(request.getPhone());
		user.setEmail(request.getEmail());
		user.setStatus(request.getStatus());
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		sysUserMapper.insert(user);
		return toInfo(user);
	}

	@Override
	public void updateUser(UserUpdateRequest request) {
		if (request.getUserId() == null) return;
		SysUser user = sysUserMapper.selectById(request.getUserId());
		if (user == null) return;
		boolean changed = false;
		if (request.getRoleId() != null && !request.getRoleId().equals(user.getRoleId())) {
			user.setRoleId(request.getRoleId()); changed = true;
		}
		if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
			user.setUsername(request.getUsername()); changed = true;
		}
		if (request.getPassword() != null && !request.getPassword().equals(user.getPassword())) {
			user.setPassword(encodePassword(request.getPassword())); changed = true;
		}
		if (request.getRealName() != null && !request.getRealName().equals(user.getRealName())) {
			user.setRealName(request.getRealName()); changed = true;
		}
		if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
			user.setPhone(request.getPhone()); changed = true;
		}
		if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
			user.setEmail(request.getEmail()); changed = true;
		}
		if (request.getStatus() != null && !request.getStatus().equals(user.getStatus())) {
			user.setStatus(request.getStatus()); changed = true;
		}
		if (changed) {
			user.setUpdatedAt(LocalDateTime.now());
			sysUserMapper.updateById(user);
		}
	}

	@Override
	public void deleteUser(Long id) {
		if (id != null) {
			sysUserMapper.deleteById(id);
		}
	}

	private UserInfo toInfo(SysUser user) {
		UserInfo info = new UserInfo();
		info.setUserId(user.getUserId());
		info.setRoleId(user.getRoleId());
		info.setUsername(user.getUsername());
		info.setRealName(user.getRealName());
		info.setPhone(user.getPhone());
		info.setEmail(user.getEmail());
		info.setStatus(user.getStatus());
		return info;
	}
}
