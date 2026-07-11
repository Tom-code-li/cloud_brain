package com.neuCloudBrainMedical.admin.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements IUserQueryService {

	private final SysUserMapper sysUserMapper;

	public UserQueryServiceImpl(SysUserMapper sysUserMapper) {
		this.sysUserMapper = sysUserMapper;
	}

	@Override
	public UserInfo findUserById(Long id) {
		if (id == null) return null;
		SysUser user = sysUserMapper.selectById(id);
		return user != null ? toInfo(user) : null;
	}

	@Override
	public Map<Long, UserInfo> findUsersByIds(Collection<Long> userIds) {
		Map<Long, UserInfo> result = new HashMap<>();
		if (userIds == null || userIds.isEmpty()) {
			return result;
		}
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(SysUser::getUserId, userIds);
		List<SysUser> users = sysUserMapper.selectList(wrapper);
		for (SysUser u : users) {
			result.put(u.getUserId(), toInfo(u));
		}
		return result;
	}

	@Override
	public boolean isPhoneTaken(String phone) {
		if (phone == null || phone.isBlank()) return false;
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysUser::getPhone, phone);
		return sysUserMapper.selectCount(wrapper) > 0;
	}

	@Override
	public boolean isEmailTaken(String email) {
		if (email == null || email.isBlank()) return false;
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysUser::getEmail, email);
		return sysUserMapper.selectCount(wrapper) > 0;
	}

	@Override
	public boolean isUsernameTaken(String username, Long excludeUserId) {
		if (username == null || username.isBlank()) return false;
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysUser::getUsername, username);
		if (excludeUserId != null) {
			wrapper.ne(SysUser::getUserId, excludeUserId);
		}
		return sysUserMapper.selectCount(wrapper) > 0;
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
