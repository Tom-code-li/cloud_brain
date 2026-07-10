package com.neuCloudBrainMedical.admin.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.service.auth.IAuthCommandService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证命令服务（写操作语义）。
 *
 * <p>登录操作虽然不改变数据库实体，但会生成新的 Token，从 CQRS 角度视为命令。</p>
 */
@Service
public class AuthCommandServiceImpl implements IAuthCommandService {

	private static final int ENABLED_STATUS = 1;

	private final SysUserMapper sysUserMapper;
	private final IRoleQueryService roleQueryService;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthCommandServiceImpl(SysUserMapper sysUserMapper,
			IRoleQueryService roleQueryService,
			JwtTokenProvider jwtTokenProvider) {
		this.sysUserMapper = sysUserMapper;
		this.roleQueryService = roleQueryService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		SysUser user = findByUsername(request.getUsername());
		if (user == null || !isEnabled(user) || !passwordMatches(request.getPassword(), user.getPassword())) {
			throw new BusinessException(401, "用户名或密码错误");
		}
		RoleInfo role = findRole(user.getRoleId());
		String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUsername(), role.getRoleCode());
		return toLoginResponse(user, role, token);
	}

	private boolean isEnabled(SysUser user) {
		return user.getStatus() != null && user.getStatus() == ENABLED_STATUS;
	}

	private SysUser findByUsername(String username) {
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysUser::getUsername, username);
		return sysUserMapper.selectOne(queryWrapper);
	}

	private RoleInfo findRole(Long roleId) {
		return roleQueryService.findRoleById(roleId);
	}

	private boolean passwordMatches(String rawPassword, String storedPassword) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
			return false;
		}
		// 数据库存的是 {noop}前缀 + 明文，验证时去掉 {noop} 前缀，用户只需输入真实密码
		String actualPassword = storedPassword.startsWith("{noop}")
				? storedPassword.substring("{noop}".length())
				: storedPassword;
		return rawPassword.equals(actualPassword);
	}

	private LoginResponse toLoginResponse(SysUser user, RoleInfo role, String token) {
		LoginResponse response = new LoginResponse();
		response.setToken(token);
		response.setUserId(user.getUserId());
		response.setUsername(user.getUsername());
		response.setRealName(user.getRealName());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		return response;
	}
}
