package com.neuCloudBrainMedical.admin.service.department.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.department.DepartmentMapper;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.department.converter.DepartmentResponseAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DepartmentQueryServiceImpl implements IDepartmentQueryService {

	private final DepartmentMapper departmentMapper;
	private final DepartmentResponseAssembler departmentResponseAssembler;

	public DepartmentQueryServiceImpl(DepartmentMapper departmentMapper, DepartmentResponseAssembler departmentResponseAssembler) {
		this.departmentMapper = departmentMapper;
		this.departmentResponseAssembler = departmentResponseAssembler;
	}

	@Override
	public List<DepartmentResponse> listDepartments(String keyword, Integer status) {
		LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
		boolean hasKeyword = keyword != null && !keyword.isBlank();
		if (hasKeyword) {
			wrapper.like(Department::getDeptName, keyword.trim());
		}
		if (status != null) {
			wrapper.eq(Department::getStatus, status);
		}
		wrapper.orderByAsc(Department::getSortOrder).orderByAsc(Department::getDeptId);
		return departmentResponseAssembler.toResponseList(departmentMapper.selectList(wrapper));
	}

	@Override
	public List<DepartmentTreeNode> getDepartmentTree() {
		LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
		wrapper.orderByAsc(Department::getSortOrder).orderByAsc(Department::getDeptId);
		return departmentResponseAssembler.toTreeNodes(departmentMapper.selectList(wrapper));
	}

	@Override
	public DepartmentResponse getDepartmentDetail(Long id) {
		Department department = departmentMapper.selectById(id);
		if (department == null) {
			throw new BusinessException(404, "科室不存在");
		}
		return departmentResponseAssembler.toResponse(department);
	}

	@Override
	public List<DepartmentResponse> getAncestors(Long id) {
		LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
		wrapper.orderByAsc(Department::getSortOrder).orderByAsc(Department::getDeptId);
		return departmentResponseAssembler.buildAncestorChain(id, departmentMapper.selectList(wrapper));
	}

	@Override
	public Map<Long, DepartmentResponse> findDepartmentResponsesByIds(Set<Long> deptIds) {
		if (deptIds == null || deptIds.isEmpty()) {
			return java.util.Collections.emptyMap();
		}
		LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(Department::getDeptId, deptIds);
		List<Department> departments = departmentMapper.selectList(wrapper);
		Map<Long, DepartmentResponse> result = new HashMap<>();
		for (Department d : departments) {
			result.put(d.getDeptId(), departmentResponseAssembler.toResponse(d));
		}
		return result;
	}

	@Override
	public long countEnabledDepartments() {
		LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Department::getStatus, Department.STATUS_ENABLED);
		Long count = departmentMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}
}
