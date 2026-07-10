package com.neuCloudBrainMedical.admin.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.user.UserCreateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.SysUser;
import com.neuCloudBrainMedical.admin.mapper.admin.SysUserMapper;
import com.neuCloudBrainMedical.admin.service.user.impl.UserCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.user.impl.UserQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

	@Mock
	private SysUserMapper sysUserMapper;

	private UserCommandServiceImpl userCommandService;
	private UserQueryServiceImpl userQueryService;

	@BeforeEach
	void setUp() {
		userCommandService = new UserCommandServiceImpl(sysUserMapper);
		userQueryService = new UserQueryServiceImpl(sysUserMapper);
	}

	@Test
	void createUserShouldEncodePasswordAndReturnInfo() {
		UserCreateRequest request = new UserCreateRequest();
		request.setRoleId(11L);
		request.setUsername("alice");
		request.setPassword("secret");
		request.setRealName("Alice");
		request.setPhone("13800000000");
		request.setEmail("alice@example.com");
		request.setStatus(1);

		ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
		when(sysUserMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
			SysUser user = invocation.getArgument(0);
			user.setUserId(99L);
			return 1;
		});

		UserInfo info = userCommandService.createUser(request);

		verify(sysUserMapper).insert(captor.capture());
		assertEquals("{noop}secret", captor.getValue().getPassword());
		assertEquals(99L, info.getUserId());
	}

	@Test
	void createUserShouldNotDoubleEncodeNoopPassword() {
		UserCreateRequest request = new UserCreateRequest();
		request.setUsername("bob");
		request.setPassword("{noop}secret");

		ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
		userCommandService.createUser(request);

		verify(sysUserMapper).insert(captor.capture());
		assertEquals("{noop}secret", captor.getValue().getPassword());
	}

	@Test
	void createUserShouldAllowNullPasswordBoundary() {
		UserCreateRequest request = new UserCreateRequest();
		request.setUsername("nullpass");
		request.setPassword(null);

		ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
		userCommandService.createUser(request);

		verify(sysUserMapper).insert(captor.capture());
		assertNull(captor.getValue().getPassword());
	}

	@Test
	void updateUserShouldDoNothingWhenIdMissing() {
		userCommandService.updateUser(new UserUpdateRequest());
		verify(sysUserMapper, never()).updateById(any(SysUser.class));
	}

	@Test
	void updateUserShouldDoNothingWhenUserMissing() {
		UserUpdateRequest request = new UserUpdateRequest();
		request.setUserId(1L);
		when(sysUserMapper.selectById(1L)).thenReturn(null);

		userCommandService.updateUser(request);

		verify(sysUserMapper, never()).updateById(any(SysUser.class));
	}

	@Test
	void updateUserShouldPatchChangedFields() {
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("old");
		user.setPassword("{noop}old");
		user.setRealName("Old");
		user.setPhone("111");
		user.setEmail("old@example.com");
		user.setStatus(1);
		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserUpdateRequest request = new UserUpdateRequest();
		request.setUserId(1L);
		request.setUsername("new");
		request.setPassword("newpass");
		request.setRealName("New");
		request.setPhone("222");
		request.setEmail("new@example.com");
		request.setStatus(0);

		userCommandService.updateUser(request);

		verify(sysUserMapper).updateById(any(SysUser.class));
	}

	@Test
	void updateUserShouldPatchRoleOnlyAndEncodeNoopPasswordWithoutDoublePrefix() {
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setRoleId(1L);
		user.setPassword("{noop}old");
		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserUpdateRequest request = new UserUpdateRequest();
		request.setUserId(1L);
		request.setRoleId(2L);
		request.setPassword("{noop}new");

		ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
		userCommandService.updateUser(request);

		verify(sysUserMapper).updateById(captor.capture());
		assertEquals(2L, captor.getValue().getRoleId());
		assertEquals("{noop}new", captor.getValue().getPassword());
	}

	@Test
	void updateUserShouldSkipWhenNothingChanged() {
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("same");
		user.setPassword("{noop}secret");
		user.setRealName("Same");
		user.setPhone("111");
		user.setEmail("same@example.com");
		user.setStatus(1);
		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserUpdateRequest request = new UserUpdateRequest();
		request.setUserId(1L);
		request.setUsername("same");
		request.setPassword("{noop}secret");
		request.setRealName("Same");
		request.setPhone("111");
		request.setEmail("same@example.com");
		request.setStatus(1);

		userCommandService.updateUser(request);

		verify(sysUserMapper, never()).updateById(any(SysUser.class));
	}

	@Test
	void findUserByIdShouldReturnNullWhenMissing() {
		assertNull(userQueryService.findUserById(null));
	}

	@Test
	void findUserByIdShouldReturnInfoWhenPresent() {
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("alice");
		when(sysUserMapper.selectById(1L)).thenReturn(user);

		UserInfo result = userQueryService.findUserById(1L);

		assertEquals("alice", result.getUsername());
	}

	@Test
	void findUserByIdShouldReturnNullWhenUserNotFound() {
		when(sysUserMapper.selectById(2L)).thenReturn(null);
		assertNull(userQueryService.findUserById(2L));
	}

	@Test
	void findUsersByIdsShouldReturnEmptyForBlankInput() {
		assertTrue(userQueryService.findUsersByIds(null).isEmpty());
		assertTrue(userQueryService.findUsersByIds(List.of()).isEmpty());
	}

	@Test
	void findUsersByIdsShouldReturnMappedUsers() {
		SysUser user = new SysUser();
		user.setUserId(1L);
		user.setUsername("alice");
		when(sysUserMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(user));

		Map<Long, UserInfo> result = userQueryService.findUsersByIds(List.of(1L));

		assertEquals(1, result.size());
		assertEquals("alice", result.get(1L).getUsername());
	}

	@Test
	void usernamePhoneAndEmailChecksShouldUseCounts() {
		when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
		assertTrue(userQueryService.isPhoneTaken("13800000000"));
		assertTrue(userQueryService.isEmailTaken("a@b.com"));
		assertTrue(userQueryService.isUsernameTaken("alice", 2L));
	}

	@Test
	void usernamePhoneAndEmailChecksShouldReturnFalseWhenCountsAreZero() {
		when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

		assertFalse(userQueryService.isPhoneTaken("13800000001"));
		assertFalse(userQueryService.isEmailTaken("free@example.com"));
		assertFalse(userQueryService.isUsernameTaken("free", null));
	}

	@Test
	void uniquenessChecksShouldIgnoreBlankValues() {
		assertFalse(userQueryService.isPhoneTaken(" "));
		assertFalse(userQueryService.isEmailTaken(""));
		assertFalse(userQueryService.isUsernameTaken(null, null));
	}

	@Test
	void deleteUserShouldDeleteOnlyWhenIdPresent() {
		userCommandService.deleteUser(null);
		verify(sysUserMapper, never()).deleteById(anyLong());

		userCommandService.deleteUser(1L);
		verify(sysUserMapper).deleteById(1L);
	}
}
