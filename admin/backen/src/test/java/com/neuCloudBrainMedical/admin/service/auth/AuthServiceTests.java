package com.neuCloudBrainMedical.admin.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.service.auth.impl.AuthCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.auth.impl.AuthQueryServiceImpl;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	private SysUserMapper sysUserMapper;
	@Mock
	private IRoleQueryService roleQueryService;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	private AuthCommandServiceImpl authCommandService;
	private AuthQueryServiceImpl authQueryService;

	@BeforeEach
	void setUp() {
		authCommandService = new AuthCommandServiceImpl(sysUserMapper, roleQueryService, jwtTokenProvider);
		authQueryService = new AuthQueryServiceImpl(sysUserMapper, roleQueryService, jwtTokenProvider);
	}

	@Test
	void loginShouldReturnTokenWhenCredentialsAreValid() {
		LoginRequest request = new LoginRequest();
		request.setUsername("admin");
		request.setPassword("secret");

		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("admin");
		user.setPassword("{noop}secret");
		user.setRealName("Admin");
		user.setRoleId(11L);
		user.setStatus(1);
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		RoleInfo role = new RoleInfo();
		role.setRoleId(11L);
		role.setRoleCode("ADMIN");
		role.setRoleName("Administrator");
		when(roleQueryService.findRoleById(11L)).thenReturn(role);
		when(jwtTokenProvider.generateToken(1L, "admin", "ADMIN")).thenReturn("token-1");

		LoginResponse response = authCommandService.login(request);

		assertEquals("token-1", response.getToken());
		assertEquals("admin", response.getUsername());
		assertEquals("Administrator", response.getRoleName());
		verify(jwtTokenProvider).generateToken(1L, "admin", "ADMIN");
	}

	@Test
	void loginShouldRejectDisabledUser() {
		LoginRequest request = new LoginRequest();
		request.setUsername("admin");
		request.setPassword("secret");

		SysUser user = new SysUser();
		user.setStatus(0);
		user.setPassword("{noop}secret");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		BusinessException exception = assertThrows(BusinessException.class, () -> authCommandService.login(request));
		assertEquals(401, exception.getCode());
	}

	@Test
	void loginShouldRejectMissingUserAndBlankPassword() {
		LoginRequest missing = new LoginRequest();
		missing.setUsername("missing");
		missing.setPassword("secret");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
		assertThrows(BusinessException.class, () -> authCommandService.login(missing));

		LoginRequest blank = new LoginRequest();
		blank.setUsername("admin");
		blank.setPassword("");
		SysUser user = new SysUser();
		user.setStatus(1);
		user.setPassword("secret");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
		assertThrows(BusinessException.class, () -> authCommandService.login(blank));
	}

	@Test
	void loginShouldRejectWrongPassword() {
		LoginRequest request = new LoginRequest();
		request.setUsername("admin");
		request.setPassword("wrong");

		SysUser user = new SysUser();
		user.setStatus(1);
		user.setPassword("{noop}secret");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		BusinessException exception = assertThrows(BusinessException.class, () -> authCommandService.login(request));
		assertEquals(401, exception.getCode());
	}

	@Test
	void loginShouldAcceptStoredPasswordWithoutNoopPrefix() {
		LoginRequest request = new LoginRequest();
		request.setUsername("admin");
		request.setPassword("secret");

		SysUser user = new SysUser();
		user.setUserId(2L);
		user.setUsername("admin");
		user.setPassword("secret");
		user.setRoleId(11L);
		user.setStatus(1);
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		RoleInfo role = new RoleInfo();
		role.setRoleCode("ADMIN");
		role.setRoleName("Administrator");
		when(roleQueryService.findRoleById(11L)).thenReturn(role);
		when(jwtTokenProvider.generateToken(2L, "admin", "ADMIN")).thenReturn("token-2");

		assertEquals("token-2", authCommandService.login(request).getToken());
	}

	@Test
	void getAdminInfoShouldReturnResponse() {
		String token = "token-1";
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("admin");
		user.setRealName("Admin");
		user.setRoleId(11L);
		user.setStatus(1);
		when(jwtTokenProvider.validateToken(token)).thenReturn(true);
		when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("admin");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		RoleInfo role = new RoleInfo();
		role.setRoleCode("ADMIN");
		role.setRoleName("Administrator");
		when(roleQueryService.findRoleById(11L)).thenReturn(role);

		AdminInfoResponse response = authQueryService.getAdminInfo("Bearer " + token);

		assertEquals("admin", response.getUsername());
		assertEquals("Administrator", response.getRoleName());
	}

	@Test
	void getAdminInfoShouldRejectMissingBearer() {
		BusinessException exception = assertThrows(BusinessException.class, () -> authQueryService.getAdminInfo(null));
		assertEquals(401, exception.getCode());
	}

	@Test
	void getAdminInfoShouldRejectInvalidToken() {
		when(jwtTokenProvider.validateToken("bad")).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> authQueryService.getAdminInfo("Bearer bad"));
		assertEquals(401, exception.getCode());
	}

	@Test
	void getAdminInfoShouldRejectMissingUser() {
		when(jwtTokenProvider.validateToken("token")).thenReturn(true);
		when(jwtTokenProvider.getUsernameFromToken("token")).thenReturn("missing");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class, () -> authQueryService.getAdminInfo("Bearer token"));
		assertEquals(401, exception.getCode());
	}

	@Test
	void getAdminInfoShouldRejectDisabledUser() {
		SysUser user = new SysUser();
		user.setStatus(0);
		when(jwtTokenProvider.validateToken("token")).thenReturn(true);
		when(jwtTokenProvider.getUsernameFromToken("token")).thenReturn("disabled");
		when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

		BusinessException exception = assertThrows(BusinessException.class, () -> authQueryService.getAdminInfo("Bearer token"));
		assertEquals(401, exception.getCode());
	}
}
