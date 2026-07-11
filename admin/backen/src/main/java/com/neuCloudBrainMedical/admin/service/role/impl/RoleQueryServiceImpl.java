package com.neuCloudBrainMedical.admin.service.role.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.SysRoleMapper;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleQueryServiceImpl implements IRoleQueryService {

	private final SysRoleMapper mapper;

	public RoleQueryServiceImpl(SysRoleMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<SysRoleResponse> listRoles() {
		LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
		wrapper.orderByAsc(SysRole::getRoleId);
		return mapper.selectList(wrapper).stream().map(this::toResponse).toList();
	}

	@Override
	public SysRoleResponse getRoleDetail(Long id) {
		SysRole role = mapper.selectById(id);
		if (role == null) {
			throw new BusinessException(404, "角色不存在");
		}
		return toResponse(role);
	}

	@Override
	public RoleInfo findRoleById(Long id) {
		SysRole role = mapper.selectById(id);
		if (role == null) {
			throw new BusinessException(400, "角色不存在");
		}
		return toInfo(role);
	}

	@Override
	public RoleInfo findRoleByCode(String roleCode) {
		if (roleCode == null || roleCode.isBlank()) return null;
		LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysRole::getRoleCode, roleCode);
		SysRole role = mapper.selectOne(wrapper);
		return role != null ? toInfo(role) : null;
	}

	@Override
	public List<RoleInfo> findRolesByCodeLike(String codePattern) {
		if (codePattern == null) return List.of();
		LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(SysRole::getRoleCode, codePattern);
		wrapper.orderByAsc(SysRole::getRoleId);
		return mapper.selectList(wrapper).stream().map(this::toInfo).toList();
	}

	@Override
	public SysRoleResponse toResponse(SysRole role) {
		SysRoleResponse resp = new SysRoleResponse();
		resp.setRoleId(role.getRoleId());
		resp.setRoleCode(role.getRoleCode());
		resp.setRoleName(role.getRoleName());
		resp.setDescription(role.getDescription());
		resp.setStatus(role.getStatus());
		return resp;
	}

	private RoleInfo toInfo(SysRole role) {
		RoleInfo info = new RoleInfo();
		info.setRoleId(role.getRoleId());
		info.setRoleCode(role.getRoleCode());
		info.setRoleName(role.getRoleName());
		info.setDescription(role.getDescription());
		info.setStatus(role.getStatus());
		return info;
	}
}
