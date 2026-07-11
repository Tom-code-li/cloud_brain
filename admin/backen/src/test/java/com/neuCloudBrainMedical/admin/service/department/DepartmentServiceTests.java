package com.neuCloudBrainMedical.admin.service.department;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.department.DepartmentMapper;
import com.neuCloudBrainMedical.admin.service.department.converter.DepartmentResponseAssembler;
import com.neuCloudBrainMedical.admin.service.department.impl.DepartmentCommandServiceImpl;
import com.neuCloudBrainMedical.admin.service.department.impl.DepartmentQueryServiceImpl;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTests {

	@Mock
	private DepartmentMapper departmentMapper;
	@Mock
	private IDeptCodeGenerator deptCodeGenerator;
	@Mock
	private IDoctorQueryService doctorQueryService;
	@Mock
	private com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService scheduleQueryService;

	private DepartmentResponseAssembler assembler;
	private DepartmentQueryServiceImpl queryService;
	private DepartmentCommandServiceImpl commandService;

	@BeforeEach
	void setUp() {
		assembler = new DepartmentResponseAssembler();
		queryService = new DepartmentQueryServiceImpl(departmentMapper, assembler);
		commandService = new DepartmentCommandServiceImpl(departmentMapper, assembler, deptCodeGenerator, doctorQueryService, scheduleQueryService);
	}

	@Test
	void listDepartmentsShouldReturnMappedResponses() {
		Department department = new Department();
		department.setDeptId(1L);
		department.setDeptName("Inner Medicine");
		when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(department));

		List<DepartmentResponse> result = queryService.listDepartments("Inner", 1);

		assertEquals(1, result.size());
		assertEquals("Inner Medicine", result.get(0).getName());
	}

	@Test
	void listDepartmentsShouldHandleBlankFilters() {
		when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

		assertTrue(queryService.listDepartments(" ", null).isEmpty());
	}

	@Test
	void assemblerShouldHandleNullAndTreeStructure() {
		assertTrue(assembler.toResponseList(null).isEmpty());
		assertNull(assembler.toResponse(null));
		assertTrue(assembler.toTreeNodes(null).isEmpty());
		assertTrue(assembler.buildAncestorChain(null, null).isEmpty());

		Department parent = new Department();
		parent.setDeptId(1L);
		parent.setDeptName("Parent");
		parent.setDeptCode("P1");
		parent.setDeptType("OUTPATIENT");

		Department child = new Department();
		child.setDeptId(2L);
		child.setParentId(1L);
		child.setDeptName("Child");
		child.setDeptCode("C1");
		child.setDeptType("LAB");

		List<DepartmentTreeNode> nodes = assembler.toTreeNodes(List.of(parent, child));
		assertEquals(1, nodes.size());
		assertEquals(1, nodes.get(0).getChildren().size());
		assertEquals("Parent", assembler.toResponse(parent).getName());
	}

	@Test
	void treeShouldReturnEmptyForNullList() {
		when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
		List<DepartmentTreeNode> result = queryService.getDepartmentTree();
		assertTrue(result.isEmpty());
	}

	@Test
	void createDepartmentShouldGenerateCodeWhenBlank() {
		DepartmentCreateRequest request = new DepartmentCreateRequest();
		request.setName("Inner Medicine");
		request.setCode(" ");
		request.setDepartmentType("OUTPATIENT");
		when(deptCodeGenerator.generateCode("Inner Medicine")).thenReturn("DEPT001");

		DepartmentResponse response = commandService.createDepartment(request);

		assertEquals("Inner Medicine", response.getName());
	}

	@Test
	void createDepartmentShouldUseExplicitCodeAndDefaultSortOrder() {
		DepartmentCreateRequest request = new DepartmentCreateRequest();
		request.setName("Lab");
		request.setCode("LAB001");
		request.setDepartmentType("LAB");
		request.setDescription("desc");
		request.setFloor("2F");
		request.setPhone("123");
		request.setParentId(1L);

		DepartmentResponse response = commandService.createDepartment(request);

		assertEquals("LAB001", response.getCode());
		assertEquals(0, response.getSortOrder());
	}

	@Test
	void updateAndToggleShouldChangeStatus() {
		Department department = new Department();
		department.setDeptId(1L);
		department.setDeptName("Old");
		department.setStatus(1);
		when(departmentMapper.selectById(1L)).thenReturn(department);

		DepartmentUpdateRequest update = new DepartmentUpdateRequest();
		update.setName("New");
		update.setStatus(0);
		DepartmentResponse response = commandService.updateDepartment(1L, update);
		assertEquals("New", response.getName());

		commandService.toggleStatus(1L);
		assertEquals(1, department.getStatus());
	}

	@Test
	void updateDepartmentShouldPatchAllOptionalFields() {
		Department department = new Department();
		department.setDeptId(4L);
		department.setDeptName("Old");
		when(departmentMapper.selectById(4L)).thenReturn(department);

		DepartmentUpdateRequest update = new DepartmentUpdateRequest();
		update.setName("New");
		update.setCode("NEW001");
		update.setDepartmentType("EXAM");
		update.setDescription("desc");
		update.setFloor("3F");
		update.setPhone("456");
		update.setParentId(2L);
		update.setSortOrder(9);
		update.setStatus(0);

		DepartmentResponse response = commandService.updateDepartment(4L, update);

		assertEquals("New", response.getName());
		assertEquals("NEW001", response.getCode());
		assertEquals("EXAM", department.getDeptType());
		assertNotNull(response.getDepartmentType());
		assertEquals(9, response.getSortOrder());
		verify(departmentMapper).updateById(department);
	}

	@Test
	void toggleStatusShouldDisableWhenCurrentStatusIsNull() {
		Department department = new Department();
		department.setDeptId(5L);
		department.setStatus(null);
		when(departmentMapper.selectById(5L)).thenReturn(department);

		commandService.toggleStatus(5L);

		assertEquals(Department.STATUS_DISABLED, department.getStatus());
		verify(departmentMapper).updateById(department);
	}

	@Test
	void deleteDepartmentShouldRejectWhenDoctorsExist() {
		Department department = new Department();
		department.setDeptId(1L);
		when(departmentMapper.selectById(1L)).thenReturn(department);
		when(doctorQueryService.countActiveDoctorsByDepartment(1L)).thenReturn(1L);

		BusinessException exception = assertThrows(BusinessException.class, () -> commandService.deleteDepartment(1L));
		assertEquals(409, exception.getCode());
	}

	@Test
	void deleteDepartmentShouldRejectWhenSchedulesExistAndDeleteOtherwise() {
		Department department = new Department();
		department.setDeptId(2L);
		when(departmentMapper.selectById(2L)).thenReturn(department);
		when(doctorQueryService.countActiveDoctorsByDepartment(2L)).thenReturn(0L);
		when(scheduleQueryService.countSchedulesByDepartment(2L)).thenReturn(1L);

		BusinessException exception = assertThrows(BusinessException.class, () -> commandService.deleteDepartment(2L));
		assertEquals(409, exception.getCode());

		when(scheduleQueryService.countSchedulesByDepartment(2L)).thenReturn(0L);
		commandService.deleteDepartment(2L);
		verify(departmentMapper).deleteById(2L);
	}

	@Test
	void findDepartmentResponsesByIdsShouldReturnMap() {
		Department department = new Department();
		department.setDeptId(1L);
		department.setDeptName("Inner Medicine");
		when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(department));

		Map<Long, DepartmentResponse> result = queryService.findDepartmentResponsesByIds(Set.of(1L));

		assertEquals(1, result.size());
	}

	@Test
	void queryServiceShouldReturnDepartmentDetailAndHandleNullCount() {
		Department department = new Department();
		department.setDeptId(3L);
		department.setDeptName("Detail Dept");
		when(departmentMapper.selectById(3L)).thenReturn(department);
		when(departmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);

		assertEquals("Detail Dept", queryService.getDepartmentDetail(3L).getName());
		assertEquals(0L, queryService.countEnabledDepartments());
	}

	@Test
	void queryServiceShouldHandleEmptyAndCounts() {
		assertTrue(queryService.findDepartmentResponsesByIds(Set.of()).isEmpty());
		assertEquals(0L, queryService.countEnabledDepartments());
	}

	@Test
	void queryAndCommandShouldHandleMissingDepartments() {
		when(departmentMapper.selectById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> queryService.getDepartmentDetail(9L));
		assertThrows(BusinessException.class, () -> commandService.updateDepartment(9L, new DepartmentUpdateRequest()));
		assertThrows(BusinessException.class, () -> commandService.toggleStatus(9L));
		assertThrows(BusinessException.class, () -> commandService.deleteDepartment(9L));
	}

	@Test
	void queryServiceShouldBuildAncestorChain() {
		Department root = new Department();
		root.setDeptId(1L);
		root.setDeptName("Root");
		Department child = new Department();
		child.setDeptId(2L);
		child.setParentId(1L);
		child.setDeptName("Child");
		when(departmentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(root, child));

		assertEquals(2, queryService.getAncestors(2L).size());
	}
}
