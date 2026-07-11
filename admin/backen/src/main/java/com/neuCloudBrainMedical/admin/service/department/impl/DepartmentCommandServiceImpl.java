package com.neuCloudBrainMedical.admin.service.department.impl;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.department.DepartmentMapper;
import com.neuCloudBrainMedical.admin.service.department.IDeptCodeGenerator;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentCommandService;
import com.neuCloudBrainMedical.admin.service.department.converter.DepartmentResponseAssembler;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 科室命令服务（写操作）。
 *
 * <p>通过 {@link IDoctorQueryService} / {@link IScheduleQueryService} 校验关联数据，
 * 避免直接依赖 Doctor 与 Schedule 模块底层 Mapper，维护边界清晰。</p>
 */
@Service
@Transactional
public class DepartmentCommandServiceImpl implements IDepartmentCommandService {

	private final DepartmentMapper departmentMapper;
	private final DepartmentResponseAssembler departmentResponseAssembler;
	private final IDeptCodeGenerator deptCodeGenerator;
	private final IDoctorQueryService doctorQueryService;
	private final IScheduleQueryService scheduleQueryService;

	public DepartmentCommandServiceImpl(DepartmentMapper departmentMapper,
			DepartmentResponseAssembler departmentResponseAssembler,
			IDeptCodeGenerator deptCodeGenerator,
			IDoctorQueryService doctorQueryService,
			IScheduleQueryService scheduleQueryService) {
		this.departmentMapper = departmentMapper;
		this.departmentResponseAssembler = departmentResponseAssembler;
		this.deptCodeGenerator = deptCodeGenerator;
		this.doctorQueryService = doctorQueryService;
		this.scheduleQueryService = scheduleQueryService;
	}

	@Override
	public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
		Department department = new Department();
		department.setDeptName(request.getName());
		// 科室编号：如果前端未传入，则根据科室名称自动生成
		String code = request.getCode();
		if (code == null || code.isBlank()) {
			code = deptCodeGenerator.generateCode(request.getName());
		}
		department.setDeptCode(code);
		department.setDeptType(request.getDepartmentType());
		department.setDescription(request.getDescription());
		department.setFloor(request.getFloor());
		department.setPhone(request.getPhone());
		department.setParentId(request.getParentId());
		department.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
		department.setStatus(Department.STATUS_ENABLED);
		department.setCreatedAt(LocalDateTime.now());
		department.setUpdatedAt(LocalDateTime.now());
		departmentMapper.insert(department);
		return departmentResponseAssembler.toResponse(department);
	}

	@Override
	public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
		Department existing = departmentMapper.selectById(id);
		if (existing == null) {
			throw new BusinessException(404, "科室不存在");
		}
		if (request.getName() != null) existing.setDeptName(request.getName());
		if (request.getCode() != null) existing.setDeptCode(request.getCode());
		if (request.getDepartmentType() != null) existing.setDeptType(request.getDepartmentType());
		if (request.getDescription() != null) existing.setDescription(request.getDescription());
		if (request.getFloor() != null) existing.setFloor(request.getFloor());
		if (request.getPhone() != null) existing.setPhone(request.getPhone());
		if (request.getParentId() != null) existing.setParentId(request.getParentId());
		if (request.getSortOrder() != null) existing.setSortOrder(request.getSortOrder());
		if (request.getStatus() != null) existing.setStatus(request.getStatus());
		existing.setUpdatedAt(LocalDateTime.now());
		departmentMapper.updateById(existing);
		return departmentResponseAssembler.toResponse(existing);
	}

	@Override
	public void toggleStatus(Long id) {
		Department existing = departmentMapper.selectById(id);
		if (existing == null) {
			throw new BusinessException(404, "科室不存在");
		}
		existing.setStatus(existing.getStatus() == null
				|| existing.getStatus() == Department.STATUS_ENABLED
				? Department.STATUS_DISABLED
				: Department.STATUS_ENABLED);
		existing.setUpdatedAt(LocalDateTime.now());
		departmentMapper.updateById(existing);
	}

	@Override
	public void deleteDepartment(Long id) {
		Department existing = departmentMapper.selectById(id);
		if (existing == null) {
			throw new BusinessException(404, "科室不存在");
		}
		long doctorCount = doctorQueryService.countActiveDoctorsByDepartment(id);
		if (doctorCount > 0) {
			throw new BusinessException(409, "该科室下仍有在职医生，不可删除");
		}
		long scheduleCount = scheduleQueryService.countSchedulesByDepartment(id);
		if (scheduleCount > 0) {
			throw new BusinessException(409, "该科室下仍有排班记录，不可删除");
		}
		departmentMapper.deleteById(id);
	}

}
