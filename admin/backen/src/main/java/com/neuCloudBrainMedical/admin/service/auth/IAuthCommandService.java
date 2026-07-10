package com.neuCloudBrainMedical.admin.service.auth;

import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;

/**
 * 认证命令服务（写操作语义）。
 *
 * 登录操作虽然不改变数据库实体，但会生成新的 Token，从 CQRS 角度视为命令。
 */
public interface IAuthCommandService {

	LoginResponse login(LoginRequest request);
}