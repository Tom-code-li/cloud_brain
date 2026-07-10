package com.neuCloudBrainMedical.admin.service.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysRoleMapper;
import com.neuCloudBrainMedical.admin.service.role.impl.RoleCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.role.impl.RoleQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTests {

	@Mock
	private SysRoleMapper mapper;

	private RoleQueryServiceImpl queryService;
	private RoleCommandServiceImpl commandService;

	@BeforeEach
	void setUp() {
		queryService = new RoleQueryServiceImpl(mapper);
		commandService = new RoleCommandServiceImpl(mapper, queryService);
	}

	@Test
	void listRolesShouldMapResponses() {
		SysRole role = new SysRole();
		role.setRoleId(1L);
		role.setRoleCode("ADMIN");
		role.setRoleName("管理员");
		when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(role));

		List<SysRoleResponse> result = queryService.listRoles();

		assertEquals(1, result.size());
		assertEquals("ADMIN", result.get(0).getRoleCode());
	}

	@Test
	void queryServiceShouldHandleNullsAndMissingRoles() {
		assertNull(queryService.findRoleByCode(null));
		assertTrue(queryService.findRolesByCodeLike(null).isEmpty());

		SysRole role = new SysRole();
		role.setRoleId(2L);
		role.setRoleCode("DOCTOR");
		role.setRoleName("医生");
		when(mapper.selectById(2L)).thenReturn(role);
		assertEquals("DOCTOR", queryService.findRoleById(2L).getRoleCode());
		assertEquals("医生", queryService.getRoleDetail(2L).getRoleName());
	}

	@Test
	void queryServiceShouldReturnNullWhenRoleMissingByCode() {
		when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
		assertNull(queryService.findRoleByCode("ADMIN"));
	}

	@Test
	void queryServiceShouldReturnRoleWhenFoundByCode() {
		SysRole role = new SysRole();
		role.setRoleId(1L);
		role.setRoleCode("ADMIN");
		role.setRoleName("Admin");
		when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(role);

		RoleInfo result = queryService.findRoleByCode("ADMIN");

		assertEquals("ADMIN", result.getRoleCode());
	}

	@Test
	void queryServiceShouldRejectMissingRoleById() {
		when(mapper.selectById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> queryService.findRoleById(9L));
		assertThrows(BusinessException.class, () -> queryService.getRoleDetail(9L));
	}

	@Test
	void findRoleByCodeShouldReturnNullForBlank() {
		assertNull(queryService.findRoleByCode(" "));
	}

	@Test
	void createRoleShouldRejectDuplicateCode() {
		SysRoleRequest request = new SysRoleRequest();
		request.setRoleCode("ADMIN");
		when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

		BusinessException exception = assertThrows(BusinessException.class, () -> commandService.createRole(request));
		assertEquals(400, exception.getCode());
	}

	@Test
	void createRoleShouldDefaultStatusWhenRequestStatusIsNull() {
		SysRoleRequest request = new SysRoleRequest();
		request.setRoleCode("USER");
		request.setRoleName("User");
		when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

		SysRoleResponse response = commandService.createRole(request);

		assertEquals("USER", response.getRoleCode());
		assertEquals(1, response.getStatus());
	}

	@Test
	void createUpdateAndDeleteShouldUseTargetRole() {
		SysRoleRequest request = new SysRoleRequest();
		request.setRoleCode("ADMIN");
		request.setRoleName("管理员");
		request.setDescription("desc");
		request.setStatus(1);
		when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
		when(mapper.insert(any(SysRole.class))).thenAnswer(invocation -> 1);

		SysRoleResponse created = commandService.createRole(request);
		assertEquals("ADMIN", created.getRoleCode());

		SysRole existing = new SysRole();
		existing.setRoleId(1L);
		existing.setRoleCode("ADMIN");
		existing.setStatus(1);
		when(mapper.selectById(1L)).thenReturn(existing);

		SysRoleRequest update = new SysRoleRequest();
		update.setRoleCode("ADMIN");
		update.setRoleName("管理员2");
		update.setDescription("desc2");
		update.setStatus(0);
		SysRoleResponse updated = commandService.updateRole(1L, update);
		assertEquals("管理员2", updated.getRoleName());

		commandService.deleteRole(1L);
		verify(mapper).deleteById(1L);
	}

	@Test
	void updateRoleShouldRejectDuplicateNewCodeAndAllowUniqueCode() {
		SysRole existing = new SysRole();
		existing.setRoleId(1L);
		existing.setRoleCode("OLD");
		existing.setRoleName("Old");
		existing.setStatus(1);
		when(mapper.selectById(1L)).thenReturn(existing);

		SysRoleRequest duplicate = new SysRoleRequest();
		duplicate.setRoleCode("NEW");
		duplicate.setRoleName("New");
		when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
		assertThrows(BusinessException.class, () -> commandService.updateRole(1L, duplicate));

		SysRoleRequest unique = new SysRoleRequest();
		unique.setRoleCode("UNIQUE");
		unique.setRoleName("Unique");
		unique.setDescription("desc");
		when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

		SysRoleResponse response = commandService.updateRole(1L, unique);

		assertEquals("UNIQUE", response.getRoleCode());
		assertEquals(1, response.getStatus());
	}

	@Test
	void createUpdateDeleteAndToggleShouldRejectMissingRoles() {
		when(mapper.selectById(7L)).thenReturn(null);
		SysRoleRequest request = new SysRoleRequest();
		request.setRoleCode("X");
		request.setRoleName("X");
		assertThrows(BusinessException.class, () -> commandService.updateRole(7L, request));
		assertThrows(BusinessException.class, () -> commandService.deleteRole(7L));
		assertThrows(BusinessException.class, () -> commandService.toggleStatus(7L));
	}

	@Test
	void toggleStatusShouldFlipState() {
		SysRole role = new SysRole();
		role.setRoleId(1L);
		role.setStatus(1);
		when(mapper.selectById(1L)).thenReturn(role);

		commandService.toggleStatus(1L);

		assertEquals(0, role.getStatus());
		verify(mapper).updateById(role);
	}

	@Test
	void toggleStatusShouldEnableWhenCurrentStatusIsNull() {
		SysRole role = new SysRole();
		role.setRoleId(2L);
		role.setStatus(null);
		when(mapper.selectById(2L)).thenReturn(role);

		commandService.toggleStatus(2L);

		assertEquals(1, role.getStatus());
		verify(mapper).updateById(role);
	}

	@Test
	void queryServiceShouldHandleEmptyLikeAndToResponse() {
		assertTrue(queryService.findRolesByCodeLike("").isEmpty());
		SysRole role = new SysRole();
		role.setRoleId(3L);
		role.setRoleCode("TEST");
		role.setRoleName("测试");
		assertEquals("TEST", queryService.toResponse(role).getRoleCode());
	}
}
