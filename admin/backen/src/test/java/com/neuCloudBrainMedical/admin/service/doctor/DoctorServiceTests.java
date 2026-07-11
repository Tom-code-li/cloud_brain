package com.neuCloudBrainMedical.admin.service.doctor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuCloudBrainMedical.admin.dto.PageResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserCreateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.doctor.DoctorMapper;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.doctor.impl.DoctorCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.doctor.impl.DoctorQueryServiceImpl;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.service.user.IUserCommandService;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTests {

	@Mock
	private DoctorMapper doctorMapper;
	@Mock
	private IScheduleQueryService scheduleQueryService;
	@Mock
	private IUserCommandService userCommandService;
	@Mock
	private IUserQueryService userQueryService;
	@Mock
	private IRoleQueryService roleQueryService;
	@Mock
	private IDoctorQueryService doctorQueryService;
	@Mock
	private IDepartmentQueryService departmentQueryService;

	private DoctorCommandServiceImpl doctorCommandService;
	private DoctorQueryServiceImpl doctorQueryServiceImpl;

	@BeforeEach
	void setUp() {
		doctorCommandService = new DoctorCommandServiceImpl(
				doctorMapper,
				scheduleQueryService,
				userCommandService,
				roleQueryService,
				userQueryService,
				doctorQueryService
		);
		doctorQueryServiceImpl = new DoctorQueryServiceImpl(
				doctorMapper,
				departmentQueryService,
				userQueryService,
				roleQueryService
		);
	}

	@Test
	void createDoctorShouldRejectTakenPhone() {
		DoctorCreateRequest request = baseCreateRequest();
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class,
				() -> doctorCommandService.createDoctor(request));

		assertEquals(400, exception.getCode());
		assertEquals("手机号已被使用", exception.getMessage());
		verify(userCommandService, never()).createUser(any());
	}

	@Test
	void createDoctorShouldRejectTakenEmail() {
		DoctorCreateRequest request = baseCreateRequest();
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(false);
		when(userQueryService.isEmailTaken("doctor@example.com")).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class,
				() -> doctorCommandService.createDoctor(request));

		assertEquals(400, exception.getCode());
		verify(userCommandService, never()).createUser(any());
	}

	@Test
	void createDoctorShouldCreateUserAndDoctorAndReturnDetail() {
		DoctorCreateRequest request = baseCreateRequest();
		request.setLoginUsername("doc001");
		request.setLoginPassword("abcdef");
		request.setRoleId(11L);
		request.setDoctorType("OUTPATIENT");
		request.setSpecialty("心血管");
		request.setIntroduction("擅长心内科");
		request.setTitle("主任医师");

		RoleInfo role = new RoleInfo();
		role.setRoleId(11L);
		role.setRoleCode("DOCTOR_CLINIC");
		role.setRoleName("门诊医生");
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(false);
		when(userQueryService.isEmailTaken("doctor@example.com")).thenReturn(false);
		when(roleQueryService.findRoleById(11L)).thenReturn(role);
		when(userCommandService.createUser(any(UserCreateRequest.class))).thenReturn(buildUserInfo());
		when(userQueryService.isUsernameTaken("doc001", 100L)).thenReturn(false);
		when(doctorQueryService.getDoctorDetail(isNull())).thenReturn(buildDoctorResponse());

		DoctorResponse response = doctorCommandService.createDoctor(request);

		assertNotNull(response);
		verify(doctorMapper).insert(any(Doctor.class));
		verify(doctorMapper).updateById(any(Doctor.class));
		ArgumentCaptor<UserCreateRequest> userCreateCaptor = ArgumentCaptor.forClass(UserCreateRequest.class);
		verify(userCommandService).createUser(userCreateCaptor.capture());
		assertEquals("abcdef", userCreateCaptor.getValue().getPassword());
		verify(userCommandService).updateUser(any());
		verify(doctorQueryService).getDoctorDetail(isNull());
	}

	@Test
	void createDoctorShouldUseDefaultRoleAndPasswordWhenOmitted() {
		DoctorCreateRequest request = baseCreateRequest();
		request.setRoleId(null);
		request.setLoginPassword(" ");
		RoleInfo defaultRole = new RoleInfo();
		defaultRole.setRoleId(1L);
		defaultRole.setRoleCode("PHARMACY");
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(false);
		when(userQueryService.isEmailTaken("doctor@example.com")).thenReturn(false);
		when(roleQueryService.findRoleByCode("DOCTOR_CLINIC")).thenReturn(defaultRole);
		when(roleQueryService.findRoleById(1L)).thenReturn(defaultRole);
		when(userCommandService.createUser(any(UserCreateRequest.class))).thenReturn(buildUserInfo());
		when(userQueryService.isUsernameTaken(any(), eq(100L))).thenReturn(false);
		when(doctorQueryService.getDoctorDetail(isNull())).thenReturn(buildDoctorResponse());

		doctorCommandService.createDoctor(request);

		ArgumentCaptor<UserCreateRequest> captor = ArgumentCaptor.forClass(UserCreateRequest.class);
		verify(userCommandService).createUser(captor.capture());
		assertEquals("123456", captor.getValue().getPassword());
		assertEquals(1L, captor.getValue().getRoleId());
	}

	@Test
	void createDoctorShouldRejectWhenNoDoctorRoleConfigured() {
		DoctorCreateRequest request = baseCreateRequest();
		request.setRoleId(null);
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(false);
		when(userQueryService.isEmailTaken("doctor@example.com")).thenReturn(false);
		when(roleQueryService.findRoleByCode("DOCTOR_CLINIC")).thenReturn(null);
		when(roleQueryService.findRolesByCodeLike("DOCTOR")).thenReturn(List.of());

		assertThrows(BusinessException.class, () -> doctorCommandService.createDoctor(request));
		verify(userCommandService, never()).createUser(any());
	}

	@Test
	void createDoctorShouldRejectDuplicateGeneratedUsername() {
		DoctorCreateRequest request = baseCreateRequest();
		request.setLoginUsername("taken");
		request.setRoleId(11L);
		RoleInfo role = new RoleInfo();
		role.setRoleId(11L);
		role.setRoleCode("UNKNOWN_ROLE");
		when(userQueryService.isPhoneTaken("13800000000")).thenReturn(false);
		when(userQueryService.isEmailTaken("doctor@example.com")).thenReturn(false);
		when(roleQueryService.findRoleById(11L)).thenReturn(role);
		when(userCommandService.createUser(any(UserCreateRequest.class))).thenReturn(buildUserInfo());
		when(userQueryService.isUsernameTaken("taken", 100L)).thenReturn(true);

		assertThrows(BusinessException.class, () -> doctorCommandService.createDoctor(request));
	}

	@Test
	void checkBeforeDisableShouldAggregateScheduleAndRegistrationCounts() {
		when(scheduleQueryService.countActiveSchedulesByDoctor(9L)).thenReturn(2L);
		when(scheduleQueryService.countPendingRegistrationsByDoctor(9L)).thenReturn(3L);

		DoctorDisableCheckResponse response = doctorCommandService.checkBeforeDisable(9L);

		assertTrue(response.hasPending());
		assertEquals(2L, response.getPendingSchedules());
		assertEquals(3L, response.getPendingRegistrations());
	}

	@Test
	void toggleStatusShouldBlockDisableWithoutForceWhenPendingExists() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(8L);
		doctor.setStatus(Doctor.STATUS_ENABLED);
		doctor.setUserId(22L);
		when(doctorMapper.selectById(8L)).thenReturn(doctor);
		when(scheduleQueryService.countActiveSchedulesByDoctor(8L)).thenReturn(1L);
		when(scheduleQueryService.countPendingRegistrationsByDoctor(8L)).thenReturn(0L);

		BusinessException exception = assertThrows(BusinessException.class,
				() -> doctorCommandService.toggleStatus(8L, false));

		assertEquals(409, exception.getCode());
		verify(doctorMapper, never()).updateById(any(Doctor.class));
		verify(userCommandService, never()).updateUser(any());
	}

	@Test
	void updateDoctorShouldReturnNotFoundWhenDoctorMissing() {
		when(doctorMapper.selectById(99L)).thenReturn(null);

		BusinessException exception = assertThrows(BusinessException.class,
				() -> doctorCommandService.updateDoctor(99L, new DoctorUpdateRequest()));

		assertEquals(404, exception.getCode());
		assertEquals("医生不存在", exception.getMessage());
	}

	@Test
	void updateDoctorShouldPatchDoctorAndRelatedUser() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(1L);
		doctor.setUserId(100L);
		doctor.setDeptId(10L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("Old");
		when(doctorMapper.selectById(1L)).thenReturn(doctor);

		UserInfo user = new UserInfo();
		user.setUserId(100L);
		user.setUsername("old");
		user.setRealName("Old");
		user.setPhone("111");
		user.setEmail("old@example.com");
		user.setRoleId(1L);
		when(userQueryService.findUserById(100L)).thenReturn(user);
		when(userQueryService.isUsernameTaken("new", 100L)).thenReturn(false);
		RoleInfo role = new RoleInfo();
		role.setRoleId(2L);
		when(roleQueryService.findRoleById(2L)).thenReturn(role);
		when(doctorQueryService.getDoctorDetail(1L)).thenReturn(buildDoctorResponse());

		DoctorUpdateRequest request = new DoctorUpdateRequest();
		request.setDepartmentId(11L);
		request.setDoctorType("LAB");
		request.setTitle("New");
		request.setSpecialty("Spec");
		request.setIntroduction("Intro");
		request.setName("New Name");
		request.setPhone("222");
		request.setEmail("new@example.com");
		request.setLoginUsername("new");
		request.setLoginPassword("secret");
		request.setRoleId(2L);

		assertNotNull(doctorCommandService.updateDoctor(1L, request));
		verify(doctorMapper).updateById(any(Doctor.class));
		verify(userCommandService).updateUser(any());
	}

	@Test
	void updateDoctorShouldSkipPersistenceWhenNothingChangedOrUserMissing() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(2L);
		doctor.setUserId(200L);
		doctor.setDeptId(10L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("Title");
		doctor.setSpecialty("Spec");
		doctor.setIntroduction("Intro");
		when(doctorMapper.selectById(2L)).thenReturn(doctor);
		when(userQueryService.findUserById(200L)).thenReturn(null);
		when(doctorQueryService.getDoctorDetail(2L)).thenReturn(buildDoctorResponse());

		DoctorUpdateRequest request = new DoctorUpdateRequest();
		request.setDepartmentId(10L);
		request.setDoctorType("OUTPATIENT");
		request.setTitle("Title");
		request.setSpecialty("Spec");
		request.setIntroduction("Intro");

		assertNotNull(doctorCommandService.updateDoctor(2L, request));
		verify(doctorMapper, never()).updateById(any(Doctor.class));
		verify(userCommandService, never()).updateUser(any());
	}

	@Test
	void updateDoctorShouldRejectDuplicateUsernameAndMissingRole() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(3L);
		doctor.setUserId(300L);
		when(doctorMapper.selectById(3L)).thenReturn(doctor);
		UserInfo user = new UserInfo();
		user.setUserId(300L);
		user.setUsername("old");
		user.setRoleId(1L);
		when(userQueryService.findUserById(300L)).thenReturn(user);

		DoctorUpdateRequest duplicateUsername = new DoctorUpdateRequest();
		duplicateUsername.setLoginUsername("taken");
		when(userQueryService.isUsernameTaken("taken", 300L)).thenReturn(true);
		assertThrows(BusinessException.class, () -> doctorCommandService.updateDoctor(3L, duplicateUsername));

		DoctorUpdateRequest missingRole = new DoctorUpdateRequest();
		missingRole.setRoleId(9L);
		when(roleQueryService.findRoleById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> doctorCommandService.updateDoctor(3L, missingRole));
	}

	@Test
	void toggleStatusShouldForceDisableAndSyncUserStatus() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(8L);
		doctor.setStatus(Doctor.STATUS_ENABLED);
		doctor.setUserId(22L);
		when(doctorMapper.selectById(8L)).thenReturn(doctor);
		when(scheduleQueryService.countActiveSchedulesByDoctor(8L)).thenReturn(1L);
		when(scheduleQueryService.countPendingRegistrationsByDoctor(8L)).thenReturn(0L);
		UserInfo user = new UserInfo();
		user.setUserId(22L);
		user.setStatus(Doctor.STATUS_ENABLED);
		when(userQueryService.findUserById(22L)).thenReturn(user);
		when(doctorQueryService.getDoctorDetail(8L)).thenReturn(buildDoctorResponse());

		doctorCommandService.toggleStatus(8L, true);

		verify(doctorMapper).updateById(any(Doctor.class));
		verify(userCommandService).updateUser(any());
	}

	@Test
	void toggleStatusShouldEnableDisabledDoctorAndSkipMissingUser() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(9L);
		doctor.setStatus(Doctor.STATUS_DISABLED);
		doctor.setUserId(null);
		when(doctorMapper.selectById(9L)).thenReturn(doctor);
		when(doctorQueryService.getDoctorDetail(9L)).thenReturn(buildDoctorResponse());

		doctorCommandService.toggleStatus(9L, false);

		assertEquals(Doctor.STATUS_ENABLED, doctor.getStatus());
		verify(userCommandService, never()).updateUser(any());
	}

	@Test
	void deleteDoctorShouldRemoveDoctorAndRelatedUser() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(6L);
		doctor.setUserId(60L);
		when(doctorMapper.selectById(6L)).thenReturn(doctor);

		doctorCommandService.deleteDoctor(6L);

		verify(doctorMapper).deleteById(6L);
		verify(userCommandService).deleteUser(60L);
	}

	@Test
	void deleteDoctorShouldRejectMissingAndAllowDoctorWithoutUser() {
		when(doctorMapper.selectById(404L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> doctorCommandService.deleteDoctor(404L));

		Doctor doctor = new Doctor();
		doctor.setDoctorId(7L);
		doctor.setUserId(null);
		when(doctorMapper.selectById(7L)).thenReturn(doctor);

		doctorCommandService.deleteDoctor(7L);

		verify(doctorMapper).deleteById(7L);
		verify(userCommandService, never()).deleteUser(isNull());
	}

	@Test
	void listDoctorRolesShouldReturnEnabledDoctorRoles() {
		RoleInfo enabled = new RoleInfo();
		enabled.setRoleId(1L);
		enabled.setRoleCode("DOCTOR_CLINIC");
		enabled.setRoleName("门诊医生");
		enabled.setDescription("desc");
		enabled.setStatus(1);
		RoleInfo disabled = new RoleInfo();
		disabled.setRoleId(2L);
		disabled.setRoleCode("DOCTOR_CHIEF");
		disabled.setRoleName("主任医生");
		disabled.setDescription("desc");
		disabled.setStatus(0);
		when(roleQueryService.findRolesByCodeLike("DOCTOR")).thenReturn(List.of(enabled, disabled));

		List<DoctorRoleOption> result = doctorQueryServiceImpl.listDoctorRoles();

		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).getRoleId());
		assertEquals("DOCTOR_CLINIC", result.get(0).getRoleCode());
	}

	@Test
	void listDoctorsShouldAssemblePageResponse() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(1L);
		doctor.setDoctorNo("D20260701001");
		doctor.setDeptId(10L);
		doctor.setUserId(100L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("主任医师");
		doctor.setSpecialty("心内科");
		doctor.setStatus(1);
		doctor.setCreatedAt(LocalDateTime.of(2026, 7, 1, 10, 0));
		doctor.setUpdatedAt(LocalDateTime.of(2026, 7, 1, 12, 0));
		when(doctorMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
			Page<Doctor> page = invocation.getArgument(0);
			page.setTotal(1);
			page.setRecords(List.of(doctor));
			return page;
		});

		DepartmentResponse dept = new DepartmentResponse();
		dept.setId(10L);
		dept.setName("心内科");
		when(departmentQueryService.findDepartmentResponsesByIds(Set.of(10L))).thenReturn(Map.of(10L, dept));

		UserInfo user = new UserInfo();
		user.setUserId(100L);
		user.setUsername("doc100");
		user.setRealName("张三");
		user.setPhone("13800000000");
		user.setEmail("doc100@example.com");
		user.setRoleId(1L);
		when(userQueryService.findUsersByIds(Set.of(100L))).thenReturn(Map.of(100L, user));

		RoleInfo role = new RoleInfo();
		role.setRoleId(1L);
		role.setRoleName("门诊医生");
		role.setStatus(1);
		when(roleQueryService.findRoleById(1L)).thenReturn(role);

		PageResponse<DoctorResponse> response = doctorQueryServiceImpl.listDoctors(10L, "心", 1, "主任", 1, 10);

		assertEquals(1L, response.getTotal());
		assertEquals(1, response.getList().size());
		assertEquals("张三", response.getList().get(0).getDoctorName());
		assertEquals("心内科", response.getList().get(0).getDepartmentName());
		assertEquals("门诊医生", response.getList().get(0).getRoleName());
	}

	@Test
	void listDoctorsShouldReturnEmptyPageWhenNoRecords() {
		when(doctorMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
			Page<Doctor> page = invocation.getArgument(0);
			page.setTotal(0);
			page.setRecords(List.of());
			return page;
		});

		PageResponse<DoctorResponse> response = doctorQueryServiceImpl.listDoctors(null, null, null, null, 1, 20);

		assertEquals(0L, response.getTotal());
		assertTrue(response.getList().isEmpty());
	}

	@Test
	void listDoctorsShouldIncludeChildDepartmentsAndSkipMissingRole() {
		DepartmentResponse parent = new DepartmentResponse();
		parent.setId(10L);
		DepartmentResponse child = new DepartmentResponse();
		child.setId(11L);
		child.setParentId(10L);
		DepartmentResponse grandChild = new DepartmentResponse();
		grandChild.setId(12L);
		grandChild.setParentId(11L);
		when(departmentQueryService.listDepartments(null, null)).thenReturn(List.of(parent, child, grandChild));

		Doctor doctor = new Doctor();
		doctor.setDoctorId(2L);
		doctor.setDoctorNo("D002");
		doctor.setDeptId(12L);
		doctor.setUserId(200L);
		doctor.setDoctorType("LAB");
		doctor.setStatus(Doctor.STATUS_ENABLED);
		when(doctorMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
			Page<Doctor> page = invocation.getArgument(0);
			page.setTotal(1);
			page.setRecords(List.of(doctor));
			return page;
		});

		DepartmentResponse dept = new DepartmentResponse();
		dept.setId(12L);
		dept.setName("Child Dept");
		when(departmentQueryService.findDepartmentResponsesByIds(Set.of(12L))).thenReturn(Map.of(12L, dept));

		UserInfo user = new UserInfo();
		user.setUserId(200L);
		user.setRealName("Dr Child");
		user.setRoleId(99L);
		when(userQueryService.findUsersByIds(Set.of(200L))).thenReturn(Map.of(200L, user));
		when(roleQueryService.findRoleById(99L)).thenThrow(new BusinessException(404, "missing"));

		PageResponse<DoctorResponse> response = doctorQueryServiceImpl.listDoctors(10L, "", Doctor.STATUS_ENABLED, " ", 1, 20);

		assertEquals(1, response.getList().size());
		assertEquals("Dr Child", response.getList().get(0).getDoctorName());
		assertNull(response.getList().get(0).getRoleName());
	}

	@Test
	void listDoctorsShouldFallBackToParentDepartmentWhenChildLookupFails() {
		when(departmentQueryService.listDepartments(null, null)).thenThrow(new RuntimeException("dept unavailable"));
		when(doctorMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
			Page<Doctor> page = invocation.getArgument(0);
			page.setTotal(0);
			page.setRecords(List.of());
			return page;
		});

		PageResponse<DoctorResponse> response = doctorQueryServiceImpl.listDoctors(10L, null, null, null, 1, 10);

		assertTrue(response.getList().isEmpty());
	}

	@Test
	void listDoctorsShouldMapRowsWithoutDepartmentOrUser() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(12L);
		doctor.setDoctorNo("D012");
		doctor.setDeptId(null);
		doctor.setUserId(null);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setStatus(Doctor.STATUS_ENABLED);
		when(doctorMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
			Page<Doctor> page = invocation.getArgument(0);
			page.setTotal(1);
			page.setRecords(List.of(doctor));
			return page;
		});
		when(userQueryService.findUsersByIds(Set.of())).thenReturn(new HashMap<>());

		PageResponse<DoctorResponse> response = doctorQueryServiceImpl.listDoctors(null, null, null, null, 1, 10);

		assertEquals(1, response.getList().size());
		assertNull(response.getList().get(0).getDoctorName());
		assertNull(response.getList().get(0).getDepartmentName());
	}

	@Test
	void listEnabledDoctorsShouldReturnFallbackNameWhenUserMissing() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(3L);
		doctor.setDoctorNo("D20260701003");
		doctor.setDeptId(20L);
		doctor.setUserId(300L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("副主任医师");
		doctor.setSpecialty("呼吸科");
		doctor.setStatus(Doctor.STATUS_ENABLED);
		when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));
		when(userQueryService.findUsersByIds(Set.of(300L))).thenReturn(Collections.emptyMap());

		List<DoctorOptionDTO> result = doctorQueryServiceImpl.listEnabledDoctors(20L);

		assertEquals(1, result.size());
		assertTrue(result.get(0).getDoctorName().contains("医生3"));
	}

	@Test
	void listEnabledDoctorsShouldHandleBlankNameAndKnownDoctorTypes() {
		Doctor lab = doctorWithType(4L, "LAB");
		lab.setTitle("");
		Doctor exam = doctorWithType(5L, "EXAM");
		exam.setTitle(null);
		when(doctorMapper.selectList(any())).thenReturn(List.of(lab, exam));

		UserInfo blankName = new UserInfo();
		blankName.setUserId(400L);
		blankName.setRealName(" ");
		UserInfo named = new UserInfo();
		named.setUserId(500L);
		named.setRealName("Dr Exam");
		when(userQueryService.findUsersByIds(Set.of(400L, 500L))).thenReturn(Map.of(400L, blankName, 500L, named));

		List<DoctorOptionDTO> result = doctorQueryServiceImpl.listEnabledDoctors(null);

		assertEquals(2, result.size());
		assertTrue(result.get(0).getDoctorName().contains("4"));
		assertEquals("Dr Exam", result.get(1).getDoctorName());
		assertNotNull(result.get(0).getDoctorType());
		assertNotNull(result.get(1).getDoctorType());
	}

	@Test
	void findDoctorInfoByIdsShouldReturnMap() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(5L);
		doctor.setDoctorNo("D20260701005");
		doctor.setDeptId(30L);
		doctor.setUserId(500L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("主治医师");
		doctor.setSpecialty("儿科");
		when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));

		UserInfo user = new UserInfo();
		user.setUserId(500L);
		user.setRealName("李四");
		when(userQueryService.findUsersByIds(Set.of(500L))).thenReturn(Map.of(500L, user));

		Map<Long, DoctorInfo> result = doctorQueryServiceImpl.findDoctorInfoByIds(Set.of(5L));

		assertEquals(1, result.size());
		assertEquals("李四", result.get(5L).getDoctorName());
	}

	@Test
	void findDoctorInfoByIdsShouldReturnEmptyForBlankInput() {
		assertTrue(doctorQueryServiceImpl.findDoctorInfoByIds(null).isEmpty());
		assertTrue(doctorQueryServiceImpl.findDoctorInfoByIds(Set.of()).isEmpty());
		verify(doctorMapper, never()).selectList(any());
	}

	@Test
	void listEnabledDoctorInfosByDepartmentShouldReturnEmptyWhenNoneFound() {
		when(doctorMapper.selectList(any())).thenReturn(List.of());

		List<DoctorInfo> result = doctorQueryServiceImpl.listEnabledDoctorInfosByDepartment(99L);

		assertTrue(result.isEmpty());
	}

	@Test
	void listEnabledDoctorInfosByDepartmentShouldMapDoctorsAndUsers() {
		Doctor pharmacy = doctorWithType(6L, "PHARMACY");
		Doctor registration = doctorWithType(7L, "REGISTRATION");
		registration.setUserId(null);
		when(doctorMapper.selectList(any())).thenReturn(List.of(pharmacy, registration));

		UserInfo user = new UserInfo();
		user.setUserId(600L);
		user.setRealName("Dr Pharmacy");
		Map<Long, UserInfo> users = new HashMap<>();
		users.put(600L, user);
		when(userQueryService.findUsersByIds(Set.of(600L))).thenReturn(users);

		List<DoctorInfo> result = doctorQueryServiceImpl.listEnabledDoctorInfosByDepartment(30L);

		assertEquals(2, result.size());
		assertEquals("Dr Pharmacy", result.get(0).getDoctorName());
		assertNull(result.get(1).getDoctorName());
		assertNotNull(result.get(0).getDoctorType());
		assertNotNull(result.get(1).getDoctorType());
	}

	@Test
	void getDoctorDetailShouldTolerateMissingDepartmentRoleAndUser() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(9L);
		doctor.setDoctorNo("D009");
		doctor.setDeptId(90L);
		doctor.setUserId(900L);
		doctor.setDoctorType("UNKNOWN");
		when(doctorMapper.selectById(9L)).thenReturn(doctor);
		when(departmentQueryService.getDepartmentDetail(90L)).thenThrow(new BusinessException(404, "missing"));
		when(userQueryService.findUserById(900L)).thenReturn(null);

		DoctorResponse response = doctorQueryServiceImpl.getDoctorDetail(9L);

		assertEquals("D009", response.getDoctorNo());
		assertNull(response.getDepartmentName());
		assertNull(response.getDoctorName());
	}

	@Test
	void getDoctorDetailShouldRejectMissingAndHandleUserWithoutRole() {
		when(doctorMapper.selectById(404L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> doctorQueryServiceImpl.getDoctorDetail(404L));

		Doctor doctor = new Doctor();
		doctor.setDoctorId(10L);
		doctor.setDoctorNo("D010");
		doctor.setUserId(1000L);
		doctor.setDoctorType(null);
		when(doctorMapper.selectById(10L)).thenReturn(doctor);

		UserInfo user = new UserInfo();
		user.setUserId(1000L);
		user.setUsername("doc10");
		user.setRealName("Dr No Role");
		user.setPhone("13800000010");
		user.setEmail("doc10@example.com");
		user.setRoleId(null);
		when(userQueryService.findUserById(1000L)).thenReturn(user);

		DoctorResponse response = doctorQueryServiceImpl.getDoctorDetail(10L);

		assertEquals("Dr No Role", response.getDoctorName());
		assertEquals("doc10", response.getLoginUsername());
		assertNull(response.getRoleName());
		assertNull(response.getDoctorType());
	}

	@Test
	void getDoctorDetailShouldIgnoreMissingRoleAndMapTimestamps() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(13L);
		doctor.setDoctorNo("D013");
		doctor.setUserId(1300L);
		doctor.setDoctorType("EXAM");
		doctor.setCreatedAt(LocalDateTime.of(2026, 7, 1, 8, 0));
		doctor.setUpdatedAt(LocalDateTime.of(2026, 7, 1, 9, 0));
		when(doctorMapper.selectById(13L)).thenReturn(doctor);

		UserInfo user = new UserInfo();
		user.setUserId(1300L);
		user.setRealName("Dr Missing Role");
		user.setRoleId(130L);
		when(userQueryService.findUserById(1300L)).thenReturn(user);
		when(roleQueryService.findRoleById(130L)).thenThrow(new BusinessException(404, "missing"));

		DoctorResponse response = doctorQueryServiceImpl.getDoctorDetail(13L);

		assertEquals("Dr Missing Role", response.getDoctorName());
		assertEquals(130L, response.getRoleId());
		assertNull(response.getRoleName());
		assertNotNull(response.getCreatedAt());
		assertNotNull(response.getUpdatedAt());
	}

	@Test
	void exportDoctorsShouldReturnEmptyAndSkipMissingAssociations() {
		when(doctorMapper.selectList(any())).thenReturn(List.of());
		assertTrue(doctorQueryServiceImpl.exportDoctors(null).isEmpty());

		Doctor doctor = new Doctor();
		doctor.setDoctorId(11L);
		doctor.setDoctorNo("D011");
		doctor.setDoctorType("OTHER");
		doctor.setDeptId(null);
		doctor.setUserId(null);
		when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));
		when(userQueryService.findUsersByIds(Set.of())).thenReturn(new HashMap<>());

		List<DoctorResponse> result = doctorQueryServiceImpl.exportDoctors(null);

		assertEquals(1, result.size());
		assertEquals("OTHER", result.get(0).getDoctorType());
		assertNull(result.get(0).getDoctorName());
		assertNull(result.get(0).getDepartmentName());
	}

	@Test
	void getDetailExportAndCountsShouldReturnMappedData() {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(1L);
		doctor.setDoctorNo("D001");
		doctor.setDeptId(10L);
		doctor.setUserId(100L);
		doctor.setDoctorType("OUTPATIENT");
		doctor.setStatus(Doctor.STATUS_ENABLED);
		when(doctorMapper.selectById(1L)).thenReturn(doctor);

		DepartmentResponse department = new DepartmentResponse();
		department.setId(10L);
		department.setName("Dept");
		when(departmentQueryService.getDepartmentDetail(10L)).thenReturn(department);

		UserInfo user = new UserInfo();
		user.setUserId(100L);
		user.setRealName("Dr A");
		user.setRoleId(1L);
		when(userQueryService.findUserById(100L)).thenReturn(user);
		RoleInfo role = new RoleInfo();
		role.setRoleId(1L);
		role.setRoleName("Doctor");
		when(roleQueryService.findRoleById(1L)).thenReturn(role);

		DoctorResponse detail = doctorQueryServiceImpl.getDoctorDetail(1L);
		assertEquals("Dr A", detail.getDoctorName());

		when(doctorMapper.selectList(any())).thenReturn(List.of(doctor));
		when(userQueryService.findUsersByIds(Set.of(100L))).thenReturn(Map.of(100L, user));
		when(departmentQueryService.findDepartmentResponsesByIds(Set.of(10L))).thenReturn(Map.of(10L, department));
		assertEquals(1, doctorQueryServiceImpl.exportDoctors(10L).size());

		when(doctorMapper.selectCount(any())).thenReturn(2L);
		assertEquals(2L, doctorQueryServiceImpl.countActiveDoctorsByDepartment(10L));
		assertEquals(Map.of(10L, 1L), doctorQueryServiceImpl.getActiveDoctorCountByDepartmentMap());
	}

	@Test
	void countActiveDoctorsByDepartmentShouldHandleNullInputsAndMapperNulls() {
		assertEquals(0L, doctorQueryServiceImpl.countActiveDoctorsByDepartment(null));
		when(doctorMapper.selectCount(any())).thenReturn(null);

		assertEquals(0L, doctorQueryServiceImpl.countActiveDoctorsByDepartment(10L));
	}

	private DoctorCreateRequest baseCreateRequest() {
		DoctorCreateRequest request = new DoctorCreateRequest();
		request.setName("张医生");
		request.setPhone("13800000000");
		request.setEmail("doctor@example.com");
		request.setDepartmentId(10L);
		request.setTitle("主任医师");
		return request;
	}

	private UserInfo buildUserInfo() {
		UserInfo info = new UserInfo();
		info.setUserId(100L);
		info.setRoleId(11L);
		info.setUsername("temp");
		info.setRealName("张医生");
		info.setStatus(Doctor.STATUS_ENABLED);
		return info;
	}

	private DoctorResponse buildDoctorResponse() {
		DoctorResponse response = new DoctorResponse();
		response.setDoctorId(1L);
		response.setDoctorName("张医生");
		return response;
	}
	private Doctor doctorWithType(Long id, String doctorType) {
		Doctor doctor = new Doctor();
		doctor.setDoctorId(id);
		doctor.setDoctorNo("D" + id);
		doctor.setDeptId(30L);
		doctor.setUserId(id * 100L);
		doctor.setDoctorType(doctorType);
		doctor.setTitle("Title");
		doctor.setSpecialty("Spec");
		doctor.setStatus(Doctor.STATUS_ENABLED);
		return doctor;
	}
}
