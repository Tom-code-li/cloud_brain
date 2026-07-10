package com.neuCloudBrainMedical.admin.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.service.auth.IAuthQueryService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证查询服务（读操作语义）。
 */
@Service
@Transactional(readOnly = true)
public class AuthQueryServiceImpl implements IAuthQueryService {

	private static final int ENABLED_STATUS = 1;
	private static final String BEARER_PREFIX = "Bearer ";

	private final SysUserMapper sysUserMapper;
	private final IRoleQueryService roleQueryService;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthQueryServiceImpl(SysUserMapper sysUserMapper,
			IRoleQueryService roleQueryService,
			JwtTokenProvider jwtTokenProvider) {
		this.sysUserMapper = sysUserMapper;
		this.roleQueryService = roleQueryService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public AdminInfoResponse getAdminInfo(String authorizationHeader) {
		String token = extractToken(authorizationHeader);
		if (!jwtTokenProvider.validateToken(token)) {
			throw new BusinessException(401, "登录已过期，请重新登录");
		}
		String username = jwtTokenProvider.getUsernameFromToken(token);
		SysUser user = findByUsername(username);
		if (user == null) {
			throw new BusinessException(401, "用户不存在");
		}
		if (!isEnabled(user)) {
			throw new BusinessException(401, "用户已被禁用");
		}
		RoleInfo role = findRole(user.getRoleId());
		return toAdminInfoResponse(user, role);
	}

	private boolean isEnabled(SysUser user) {
		return user.getStatus() != null && user.getStatus() == ENABLED_STATUS;
	}

	private String extractToken(String authorizationHeader) {
		if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			throw new BusinessException(401, "请先登录");
		}
		return authorizationHeader.substring(BEARER_PREFIX.length());
	}

	private SysUser findByUsername(String username) {
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysUser::getUsername, username);
		return sysUserMapper.selectOne(queryWrapper);
	}

	private RoleInfo findRole(Long roleId) {
		return roleQueryService.findRoleById(roleId);
	}

	private AdminInfoResponse toAdminInfoResponse(SysUser user, RoleInfo role) {
		AdminInfoResponse response = new AdminInfoResponse();
		response.setUserId(user.getUserId());
		response.setUsername(user.getUsername());
		response.setRealName(user.getRealName());
		response.setRoleCode(role.getRoleCode());
		response.setRoleName(role.getRoleName());
		return response;
	}
}
