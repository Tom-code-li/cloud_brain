package com.neuCloudBrainMedical.admin.service.role.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysRoleMapper;
import com.neuCloudBrainMedical.admin.service.role.IRoleCommandService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class RoleCommandServiceImpl implements IRoleCommandService {

	private final SysRoleMapper mapper;
	private final IRoleQueryService queryService;

	public RoleCommandServiceImpl(SysRoleMapper mapper, IRoleQueryService queryService) {
		this.mapper = mapper;
		this.queryService = queryService;
	}

	@Override
	public SysRoleResponse createRole(SysRoleRequest request) {
		LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysRole::getRoleCode, request.getRoleCode());
		if (mapper.selectCount(wrapper) > 0) {
			throw new BusinessException(400, "角色编码【" + request.getRoleCode() + "】已存在");
		}
		SysRole role = new SysRole();
		role.setRoleCode(request.getRoleCode());
		role.setRoleName(request.getRoleName());
		role.setDescription(request.getDescription());
		role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
		role.setCreatedAt(LocalDateTime.now());
		role.setUpdatedAt(LocalDateTime.now());
		mapper.insert(role);
		return queryService.toResponse(role);
	}

	@Override
	public SysRoleResponse updateRole(Long id, SysRoleRequest request) {
		SysRole role = mapper.selectById(id);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}
		if (!role.getRoleCode().equals(request.getRoleCode())) {
			LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(SysRole::getRoleCode, request.getRoleCode());
			wrapper.ne(SysRole::getRoleId, id);
			if (mapper.selectCount(wrapper) > 0) {
				throw new BusinessException(400, "角色编码【" + request.getRoleCode() + "】已存在");
			}
			role.setRoleCode(request.getRoleCode());
		}
		role.setRoleName(request.getRoleName());
		role.setDescription(request.getDescription());
		if (request.getStatus() != null) role.setStatus(request.getStatus());
		role.setUpdatedAt(LocalDateTime.now());
		mapper.updateById(role);
		return queryService.toResponse(role);
	}

	@Override
	public void deleteRole(Long id) {
		SysRole role = mapper.selectById(id);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}
		mapper.deleteById(id);
	}

	@Override
	public void toggleStatus(Long id) {
		SysRole role = mapper.selectById(id);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}
		role.setStatus(role.getStatus() != null && role.getStatus() == 1 ? 0 : 1);
		role.setUpdatedAt(LocalDateTime.now());
		mapper.updateById(role);
	}
}
