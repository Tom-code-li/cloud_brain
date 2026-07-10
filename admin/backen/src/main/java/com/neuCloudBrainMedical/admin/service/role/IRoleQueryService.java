package com.neuCloudBrainMedical.admin.service.role;

import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.entity.SysRole;

import java.util.List;

/**
 * 角色查询服务（读操作语义）。
 *
 * <p>通过 RoleInfo / SysRoleResponse DTO 返回角色信息，避免直接暴露 SysRole Entity。</p>
 */
public interface IRoleQueryService {

	List<SysRoleResponse> listRoles();

	SysRoleResponse getRoleDetail(Long id);

	/** 根据 ID 查询角色信息（不存在时抛 BusinessException）。 */
	RoleInfo findRoleById(Long id);

	/** 根据编码查询角色信息（不存在时返回 null）。 */
	RoleInfo findRoleByCode(String roleCode);

	/** 查询角色编码包含指定字符串的角色列表。 */
	List<RoleInfo> findRolesByCodeLike(String codePattern);

	/** 将角色 Entity 转换为响应 DTO（供同模块 CommandService 复用）。 */
	SysRoleResponse toResponse(SysRole role);
}