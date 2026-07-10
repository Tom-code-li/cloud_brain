package com.neuCloudBrainMedical.admin.service.role;

import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;

/**
 * 角色命令服务（写操作语义）。
 */
public interface IRoleCommandService {

	SysRoleResponse createRole(SysRoleRequest request);

	SysRoleResponse updateRole(Long id, SysRoleRequest request);

	void deleteRole(Long id);

	void toggleStatus(Long id);
}