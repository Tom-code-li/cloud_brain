package com.neuCloudBrainMedical.admin.service.auth;

import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;

/**
 * 认证查询服务（读操作语义）。
 */
public interface IAuthQueryService {

	AdminInfoResponse getAdminInfo(String authorizationHeader);
}