package com.neuCloudBrainMedical.admin.service.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.Registration;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.ScheduleMapper;
import com.neuCloudBrainMedical.admin.mapper.biz.RegistrationMapper;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.impl.ScheduleCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.schedule.impl.ScheduleQueryServiceImpl;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTests {

	@Mock
	private ScheduleMapper scheduleMapper;
	@Mock
	private RegistrationMapper registrationMapper;
	@Mock
	private IDoctorQueryService doctorQueryService;
	@Mock
	private IDepartmentQueryService departmentQueryService;
	@Mock
	private IUserQueryService userQueryService;

	private ScheduleQueryServiceImpl queryService;
	private ScheduleCommandServiceImpl commandService;

	@BeforeEach
	void setUp() {
		queryService = new ScheduleQueryServiceImpl(scheduleMapper, registrationMapper, doctorQueryService, departmentQueryService, userQueryService);
		commandService = new ScheduleCommandServiceImpl(scheduleMapper, doctorQueryService, queryService);
	}

	@Test
	void createScheduleShouldPersistAndReturnResponse() {
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
		when(doctorQueryService.findDoctorInfoByIds(any())).thenReturn(Map.of());
		when(scheduleMapper.insert(any(DoctorSchedule.class))).thenAnswer(invocation -> {
			DoctorSchedule schedule = invocation.getArgument(0);
			schedule.setScheduleId(1L);
			return 1;
		});

		ScheduleResponse response = commandService.createSchedule(baseCreateRequest());

		assertEquals(1L, response.getId());
		verify(scheduleMapper).insert(any(DoctorSchedule.class));
	}

	@Test
	void batchCreateShouldRejectDuplicateRequestItems() {
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
		ScheduleBatchCreateRequest request = new ScheduleBatchCreateRequest();
		request.setSchedules(List.of(baseCreateRequest(), baseCreateRequest()));

		BusinessException exception = assertThrows(BusinessException.class, () -> commandService.batchCreateSchedule(request));
		assertEquals(409, exception.getCode());
	}

	@Test
	void batchCreateShouldRejectEmptyInputAndPersistValidItems() {
		ScheduleBatchCreateRequest empty = new ScheduleBatchCreateRequest();
		empty.setSchedules(List.of());
		assertThrows(BusinessException.class, () -> commandService.batchCreateSchedule(empty));

		ScheduleCreateRequest first = baseCreateRequest();
		ScheduleCreateRequest second = baseCreateRequest();
		second.setTimeSlot("AFTERNOON");
		ScheduleBatchCreateRequest request = new ScheduleBatchCreateRequest();
		request.setSchedules(List.of(first, second));
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
		when(doctorQueryService.findDoctorInfoByIds(any())).thenReturn(Map.of());
		when(scheduleMapper.insert(any(DoctorSchedule.class))).thenAnswer(invocation -> {
			DoctorSchedule schedule = invocation.getArgument(0);
			schedule.setScheduleId(schedule.getTimePeriod().equals("AFTERNOON") ? 2L : 1L);
			return 1;
		});

		List<ScheduleResponse> result = commandService.batchCreateSchedule(request);

		assertEquals(2, result.size());
		verify(scheduleMapper, never()).deleteById(anyLong());
	}

	@Test
	void createScheduleShouldRejectExistingConflictWithDoctorLabel() {
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(10L);
		doctor.setDoctorNo("D010");
		when(doctorQueryService.findDoctorInfoByIds(Set.of(10L))).thenReturn(Map.of(10L, doctor));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> commandService.createSchedule(baseCreateRequest()));

		assertEquals(409, exception.getCode());
		verify(scheduleMapper, never()).insert(any(DoctorSchedule.class));
	}

	@Test
	void updateScheduleShouldValidateQuotaAndPersistChanges() {
		DoctorSchedule schedule = schedule();
		when(scheduleMapper.selectById(1L)).thenReturn(schedule);

		ScheduleUpdateRequest request = new ScheduleUpdateRequest();
		request.setMaxAppointments(12);
		request.setCurrentAppointments(2);
		request.setRegistrationFee(new BigDecimal("30.00"));
		request.setStatus("OPEN");

		ScheduleResponse response = commandService.updateSchedule(1L, request);

		assertEquals(12, response.getMaxAppointments());
		assertEquals(2, response.getCurrentAppointments());
		verify(scheduleMapper).updateById(schedule);
	}

	@Test
	void updateScheduleShouldChangeTimeSlotAndRejectNegativeMaximum() {
		DoctorSchedule schedule = schedule();
		when(scheduleMapper.selectById(1L)).thenReturn(schedule);
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
		when(doctorQueryService.findDoctorInfoByIds(Set.of(10L))).thenReturn(Map.of());

		ScheduleUpdateRequest changeTime = new ScheduleUpdateRequest();
		changeTime.setTimeSlot("AFTERNOON");
		ScheduleResponse response = commandService.updateSchedule(1L, changeTime);

		assertNotEquals("MORNING", schedule.getTimePeriod());
		assertNotNull(response.getTimeSlot());
		verify(scheduleMapper).updateById(schedule);

		DoctorSchedule second = schedule();
		when(scheduleMapper.selectById(2L)).thenReturn(second);
		ScheduleUpdateRequest negative = new ScheduleUpdateRequest();
		negative.setMaxAppointments(-1);
		assertThrows(BusinessException.class, () -> commandService.updateSchedule(2L, negative));
	}

	@Test
	void updateScheduleShouldRejectMissingAndInvalidQuota() {
		when(scheduleMapper.selectById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> commandService.updateSchedule(9L, new ScheduleUpdateRequest()));

		when(scheduleMapper.selectById(1L)).thenReturn(schedule());
		ScheduleUpdateRequest request = new ScheduleUpdateRequest();
		request.setMaxAppointments(1);
		request.setCurrentAppointments(2);
		assertThrows(BusinessException.class, () -> commandService.updateSchedule(1L, request));
	}

	@Test
	void cancelScheduleShouldRejectExistingAppointmentsAndDeleteEmptySchedule() {
		DoctorSchedule withAppointment = schedule();
		withAppointment.setRemainQuota(9);
		when(scheduleMapper.selectById(1L)).thenReturn(withAppointment);
		assertThrows(BusinessException.class, () -> commandService.cancelSchedule(1L));

		DoctorSchedule empty = schedule();
		empty.setScheduleId(2L);
		when(scheduleMapper.selectById(2L)).thenReturn(empty);
		commandService.cancelSchedule(2L);
		verify(scheduleMapper).deleteById(2L);
	}

	@Test
	void cancelScheduleShouldRejectMissingSchedule() {
		when(scheduleMapper.selectById(404L)).thenReturn(null);

		assertThrows(BusinessException.class, () -> commandService.cancelSchedule(404L));
	}

	@Test
	void queryServiceShouldListSchedulesWithAssociatedDoctorAndDepartment() {
		DoctorSchedule schedule = schedule();
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of(schedule));

		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(10L);
		doctor.setDoctorName("Dr A");
		doctor.setDeptId(20L);
		when(doctorQueryService.findDoctorInfoByIds(Set.of(10L))).thenReturn(Map.of(10L, doctor));

		DepartmentResponse department = new DepartmentResponse();
		department.setId(20L);
		department.setName("Dept");
		when(departmentQueryService.findDepartmentResponsesByIds(Set.of(20L))).thenReturn(Map.of(20L, department));

		List<ScheduleResponse> result = queryService.listSchedules(20L, LocalDate.now(), LocalDate.now().plusDays(1));

		assertEquals(1, result.size());
		assertEquals("Dr A", result.get(0).getDoctorName());
		assertEquals("Dept", result.get(0).getDepartmentName());
	}

	@Test
	void queryServiceShouldExpireOutdatedActiveSchedulesBeforeListing() {
		DoctorSchedule expired = schedule();
		expired.setWorkDate(LocalDate.now().minusDays(1));
		expired.setStatus(DoctorSchedule.STATUS_ACTIVE);
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(expired), List.of());

		assertTrue(queryService.listSchedules(null, null, null).isEmpty());

		assertEquals(DoctorSchedule.STATUS_EXPIRED, expired.getStatus());
		assertNotNull(expired.getUpdatedAt());
		verify(scheduleMapper).updateById(expired);
	}

	@Test
	void queryServiceShouldListSchedulesWithoutDoctorsOrDepartments() {
		DoctorSchedule noDoctor = schedule();
		noDoctor.setScheduleId(5L);
		noDoctor.setDoctorId(null);
		noDoctor.setRemainQuota(null);
		DoctorSchedule doctorWithoutDept = schedule();
		doctorWithoutDept.setScheduleId(6L);
		doctorWithoutDept.setDoctorId(11L);
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of(noDoctor, doctorWithoutDept));

		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(11L);
		doctor.setDoctorName("Dr No Dept");
		doctor.setDeptId(null);
		Map<Long, DoctorInfo> doctors = new HashMap<>();
		doctors.put(11L, doctor);
		when(doctorQueryService.findDoctorInfoByIds(Set.of(11L))).thenReturn(doctors);

		List<ScheduleResponse> result = queryService.listSchedules(null, null, null);

		assertEquals(2, result.size());
		assertNull(result.get(0).getDoctorName());
		assertEquals(0, result.get(0).getCurrentAppointments());
		assertEquals("Dr No Dept", result.get(1).getDoctorName());
		assertNull(result.get(1).getDepartmentName());
	}

	@Test
	void queryServiceShouldListSchedulesWhenAllDoctorIdsAreMissing() {
		DoctorSchedule noDoctor = schedule();
		noDoctor.setScheduleId(7L);
		noDoctor.setDoctorId(null);
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of(noDoctor));

		List<ScheduleResponse> result = queryService.listSchedules(null, null, null);

		assertEquals(1, result.size());
		assertNull(result.get(0).getDoctorName());
	}

	@Test
	void queryServiceShouldListRegistrationsWithPatientNames() {
		Registration registration = new Registration();
		registration.setRegistrationId(1L);
		registration.setScheduleId(1L);
		registration.setPatientId(3L);
		when(registrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(registration));

		UserInfo user = new UserInfo();
		user.setUserId(3L);
		user.setRealName("Patient");
		when(registrationMapper.getUserIdMapByPatientIds(List.of(3L))).thenReturn(Map.of(3L, Map.of("user_id", 3L)));
		when(userQueryService.findUsersByIds(Set.of(3L))).thenReturn(Map.of(3L, user));

		assertEquals("Patient", queryService.listRegistrationsByScheduleId(1L).get(0).getPatientName());
	}

	@Test
	void queryServiceShouldReturnDetailAndRejectMissingSchedule() {
		when(scheduleMapper.selectById(1L)).thenReturn(schedule());
		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(10L);
		doctor.setDoctorName("Dr A");
		doctor.setDeptId(20L);
		when(doctorQueryService.findDoctorInfoByIds(Set.of(10L))).thenReturn(Map.of(10L, doctor));
		DepartmentResponse department = new DepartmentResponse();
		department.setId(20L);
		department.setName("Dept");
		when(departmentQueryService.getDepartmentDetail(20L)).thenReturn(department);

		assertEquals("Dr A", queryService.getScheduleDetail(1L).getDoctorName());

		when(scheduleMapper.selectById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> queryService.getScheduleDetail(9L));
	}

	@Test
	void queryServiceShouldMapScheduleWithoutQuotaDoctorOrDepartment() {
		DoctorSchedule noDoctor = schedule();
		noDoctor.setScheduleId(3L);
		noDoctor.setDoctorId(null);
		noDoctor.setTotalQuota(null);
		noDoctor.setRemainQuota(null);

		ScheduleResponse noDoctorResponse = queryService.toResponse(noDoctor);
		assertEquals(3L, noDoctorResponse.getId());
		assertNull(noDoctorResponse.getDoctorName());
		assertNull(noDoctorResponse.getCurrentAppointments());

		DoctorSchedule doctorWithoutDepartment = schedule();
		doctorWithoutDepartment.setScheduleId(4L);
		doctorWithoutDepartment.setRemainQuota(null);
		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(10L);
		doctor.setDoctorName("Dr A");
		doctor.setDeptId(20L);
		when(doctorQueryService.findDoctorInfoByIds(Set.of(10L))).thenReturn(Map.of(10L, doctor));
		when(departmentQueryService.getDepartmentDetail(20L)).thenThrow(new BusinessException(404, "missing"));

		ScheduleResponse response = queryService.toResponse(doctorWithoutDepartment);

		assertEquals(0, response.getCurrentAppointments());
		assertEquals("Dr A", response.getDoctorName());
		assertNull(response.getDepartmentName());
	}

	@Test
	void queryServiceShouldCountHelpers() {
		assertEquals(0L, queryService.countActiveSchedulesByDoctor(null));
		assertEquals(0L, queryService.countPendingRegistrationsByDoctor(null));
		assertEquals(0L, queryService.countSchedulesByDepartment(null));
		assertEquals(0L, queryService.countDistinctDoctorsByDateAndStatuses(null, Set.of("OPEN")));
		assertTrue(queryService.countRegistrationsByScheduleIds(Set.of()).isEmpty());

		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
		assertEquals(2L, queryService.countSchedulesByDepartment(20L));
		assertEquals(2L, queryService.countActiveSchedulesByDoctor(10L));
	}

	@Test
	void queryServiceShouldReturnZeroWhenMapperCountsAreNull() {
		when(scheduleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);
		assertEquals(0L, queryService.countSchedulesByDepartment(20L));
		assertEquals(0L, queryService.countActiveSchedulesByDoctor(10L));

		when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);
		assertEquals(0L, queryService.countRegistrationsByTimeRange(java.time.LocalDateTime.now().minusDays(1), java.time.LocalDateTime.now()));
	}

	@Test
	void queryServiceShouldCountRegistrationsAndDoctors() {
		DoctorSchedule one = schedule();
		DoctorSchedule two = schedule();
		two.setScheduleId(2L);
		two.setDoctorId(11L);
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(one, two));
		assertEquals(2L, queryService.countDistinctDoctorsByDateAndStatuses(LocalDate.now(), Set.of("OPEN")));

		Registration r1 = new Registration();
		r1.setScheduleId(1L);
		Registration r2 = new Registration();
		r2.setScheduleId(1L);
		when(registrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1, r2));
		assertEquals(Map.of(1L, 2L), queryService.countRegistrationsByScheduleIds(Set.of(1L)));

		when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
		assertEquals(5L, queryService.countRegistrationsByTimeRange(java.time.LocalDateTime.now().minusDays(1), java.time.LocalDateTime.now()));
	}

	@Test
	void queryServiceShouldCountPendingRegistrationsByDoctor() {
		DoctorSchedule schedule = schedule();
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(schedule));
		when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

		assertEquals(3L, queryService.countPendingRegistrationsByDoctor(10L));
	}

	@Test
	void queryServiceShouldReturnEmptyRegistrationsAndHandleMissingPatientNames() {
		when(registrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
		assertTrue(queryService.listRegistrationsByScheduleId(1L).isEmpty());

		Registration registration = new Registration();
		registration.setRegistrationId(2L);
		registration.setScheduleId(1L);
		registration.setPatientId(null);
		when(registrationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(registration));

		assertNull(queryService.listRegistrationsByScheduleId(1L).get(0).getPatientName());
	}

	@Test
	void queryServiceShouldHandlePendingRegistrationAndDistinctDoctorBoundaries() {
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
		assertEquals(0L, queryService.countPendingRegistrationsByDoctor(10L));

		DoctorSchedule noDoctor = schedule();
		noDoctor.setDoctorId(null);
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(noDoctor));
		assertEquals(0L, queryService.countDistinctDoctorsByDateAndStatuses(LocalDate.now(), Set.of("OPEN")));

		when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);
		assertEquals(0L, queryService.countPendingRegistrationsByDoctor(10L));
	}

	@Test
	void queryServiceShouldReturnZeroForEmptyDistinctDoctorQuery() {
		when(scheduleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

		assertEquals(0L, queryService.countDistinctDoctorsByDateAndStatuses(LocalDate.now(), Set.of("OPEN")));
	}

	private ScheduleCreateRequest baseCreateRequest() {
		ScheduleCreateRequest request = new ScheduleCreateRequest();
		request.setDoctorId(10L);
		request.setDepartmentId(20L);
		request.setScheduleDate(LocalDate.now().plusDays(1));
		request.setTimeSlot("MORNING");
		request.setMaxAppointments(10);
		return request;
	}

	private DoctorSchedule schedule() {
		DoctorSchedule schedule = new DoctorSchedule();
		schedule.setScheduleId(1L);
		schedule.setDoctorId(10L);
		schedule.setDeptId(20L);
		schedule.setWorkDate(LocalDate.now().plusDays(1));
		schedule.setTimePeriod("MORNING");
		schedule.setTotalQuota(10);
		schedule.setRemainQuota(10);
		schedule.setStatus("OPEN");
		schedule.setRegistrationFee(new BigDecimal("20.00"));
		return schedule;
	}
}
