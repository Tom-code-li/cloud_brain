package com.neuCloudBrainMedical.admin.controller;

import com.neuCloudBrainMedical.admin.controller.department.DepartmentController;
import com.neuCloudBrainMedical.admin.controller.doctor.DoctorAdminController;
import com.neuCloudBrainMedical.admin.controller.role.SysRoleController;
import com.neuCloudBrainMedical.admin.controller.schedule.AIScheduleSuggestionController;
import com.neuCloudBrainMedical.admin.controller.schedule.ScheduleController;
import com.neuCloudBrainMedical.admin.dto.PageResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleBatchCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleCreateRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleUpdateRequest;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentCommandService;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorCommandService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.role.IRoleCommandService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IAISuggestionCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerDelegationTests {

	@Test
	void departmentControllerShouldDelegateAllEndpoints() {
		IDepartmentQueryService query = mock(IDepartmentQueryService.class);
		IDepartmentCommandService command = mock(IDepartmentCommandService.class);
		DepartmentController controller = new DepartmentController(query, command);
		DepartmentResponse response = new DepartmentResponse();
		when(query.listDepartments("x", 1)).thenReturn(List.of(response));
		when(query.getDepartmentDetail(1L)).thenReturn(response);
		when(command.createDepartment(org.mockito.ArgumentMatchers.any())).thenReturn(response);
		when(command.updateDepartment(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);

		assertEquals(1, controller.listDepartments("x", 1).getData().size());
		controller.getDepartmentTree();
		controller.getDepartmentDetail(1L);
		controller.createDepartment(new DepartmentCreateRequest());
		controller.updateDepartment(1L, new DepartmentUpdateRequest());
		controller.toggleStatus(1L);
		controller.deleteDepartment(1L);

		verify(command).toggleStatus(1L);
		verify(command).deleteDepartment(1L);
	}

	@Test
	void roleControllerShouldDelegateAllEndpoints() {
		IRoleQueryService query = mock(IRoleQueryService.class);
		IRoleCommandService command = mock(IRoleCommandService.class);
		SysRoleController controller = new SysRoleController(query, command);
		SysRoleResponse response = new SysRoleResponse();
		when(query.listRoles()).thenReturn(List.of(response));
		when(query.getRoleDetail(1L)).thenReturn(response);
		when(command.createRole(org.mockito.ArgumentMatchers.any())).thenReturn(response);
		when(command.updateRole(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);

		controller.listRoles();
		controller.getRoleDetail(1L);
		controller.createRole(new SysRoleRequest());
		controller.updateRole(1L, new SysRoleRequest());
		controller.deleteRole(1L);
		controller.toggleStatus(1L);

		verify(command).deleteRole(1L);
		verify(command).toggleStatus(1L);
	}

	@Test
	void doctorControllerShouldDelegateAllEndpoints() {
		IDoctorQueryService query = mock(IDoctorQueryService.class);
		IDoctorCommandService command = mock(IDoctorCommandService.class);
		DoctorAdminController controller = new DoctorAdminController(query, command);
		DoctorResponse response = new DoctorResponse();
		when(query.listDoctors(null, "", null, null, 1, 10)).thenReturn(PageResponse.of(1, 1, 10, List.of(response)));
		when(query.getDoctorDetail(1L)).thenReturn(response);
		when(query.exportDoctors(null)).thenReturn(List.of(response));
		when(command.checkBeforeDisable(1L)).thenReturn(new DoctorDisableCheckResponse(0, 0));
		when(command.createDoctor(org.mockito.ArgumentMatchers.any())).thenReturn(response);
		when(command.updateDoctor(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);
		when(command.toggleStatus(1L, true)).thenReturn(response);

		controller.listDoctorRoles();
		controller.listDoctors(null, "", null, null, 1, 10);
		controller.getDoctorDetail(1L);
		controller.exportDoctors(null);
		controller.listEnabledDoctors(null);
		controller.checkBeforeDisable(1L);
		controller.createDoctor(new DoctorCreateRequest());
		controller.updateDoctor(1L, new DoctorUpdateRequest());
		controller.toggleStatus(1L, true);
		controller.deleteDoctor(1L);

		verify(command).deleteDoctor(1L);
	}

	@Test
	void scheduleControllerShouldDelegateAllEndpoints() {
		IScheduleQueryService query = mock(IScheduleQueryService.class);
		IScheduleCommandService command = mock(IScheduleCommandService.class);
		ScheduleController controller = new ScheduleController(query, command);
		LocalDate today = LocalDate.now();
		ScheduleResponse response = new ScheduleResponse();
		when(query.listSchedules(null, today, today)).thenReturn(List.of(response));
		when(query.getScheduleDetail(1L)).thenReturn(response);
		when(command.createSchedule(org.mockito.ArgumentMatchers.any())).thenReturn(response);
		when(command.batchCreateSchedule(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(response));
		when(command.updateSchedule(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(response);

		controller.listSchedules(null, today, today);
		controller.getScheduleDetail(1L);
		controller.listRegistrations(1L);
		controller.createSchedule(new ScheduleCreateRequest());
		controller.batchCreateSchedule(new ScheduleBatchCreateRequest());
		controller.updateSchedule(1L, new ScheduleUpdateRequest());
		controller.cancelSchedule(1L);

		verify(command).cancelSchedule(1L);
	}

	@Test
	void aiSuggestionControllerShouldDelegateAllEndpoints() {
		IAISuggestionCommandService service = mock(IAISuggestionCommandService.class);
		AIScheduleSuggestionController controller = new AIScheduleSuggestionController(service);
		ScheduleResponse schedule = new ScheduleResponse();
		when(service.generateSuggestion(org.mockito.ArgumentMatchers.any())).thenReturn(new AIScheduleSuggestionResponse());
		when(service.acceptSuggestion(1L)).thenReturn(List.of(schedule));
		when(service.acceptSuggestionDetail(1L, 2L)).thenReturn(schedule);

		controller.generateSuggestion(new AIScheduleSuggestRequest());
		controller.acceptSuggestion(1L);
		controller.rejectSuggestion(1L);
		controller.acceptSuggestionDetail(1L, 2L);
		controller.rejectSuggestionDetail(1L, 2L);

		verify(service).rejectSuggestion(1L);
		verify(service).rejectSuggestionDetail(1L, 2L);
	}
}
