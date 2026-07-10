package com.neuCloudBrainMedical.admin.service.user;

import com.neuCloudBrainMedical.admin.dto.user.UserCreateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserUpdateRequest;

/**
 * 用户命令服务（写操作语义）。
 *
 * <p>跨模块使用 UserInfo / UserCreateRequest / UserUpdateRequest DTO 进行通信，
 * 避免直接暴露 SysUser Entity。</p>
 */
public interface IUserCommandService {

	/** 创建用户（自动设置创建/更新时间），返回创建后的用户信息。 */
	UserInfo createUser(UserCreateRequest request);

	/** 更新用户（按字段更新，null 字段不更新）。 */
	void updateUser(UserUpdateRequest request);

	/** 根据 ID 删除用户。 */
	void deleteUser(Long id);
}