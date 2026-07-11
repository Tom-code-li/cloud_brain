package com.neuCloudBrainMedical.admin.service.user;

import com.neuCloudBrainMedical.admin.dto.user.UserInfo;

import java.util.Collection;
import java.util.Map;

/**
 * 用户查询服务（读操作语义）。
 *
 * <p>通过 UserInfo DTO 返回用户信息，避免直接暴露 SysUser Entity。</p>
 */
public interface IUserQueryService {

	/** 根据 ID 查询用户信息（返回 null 表示不存在）。 */
	UserInfo findUserById(Long id);

	/** 批量根据 ID 查询，返回 userId -> UserInfo 的 Map。 */
	Map<Long, UserInfo> findUsersByIds(Collection<Long> userIds);

	/** 手机号是否已被使用。 */
	boolean isPhoneTaken(String phone);

	/** 邮箱是否已被使用。 */
	boolean isEmailTaken(String email);

	/** 登录账号是否已被使用（排除指定用户 ID）。 */
	boolean isUsernameTaken(String username, Long excludeUserId);
}