package com.neuCloudBrainMedical.admin.controller.auth;

import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;
import com.neuCloudBrainMedical.admin.service.auth.IAuthCommandService;
import com.neuCloudBrainMedical.admin.service.auth.IAuthQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final IAuthCommandService authCommandService;
	private final IAuthQueryService authQueryService;

	public AuthController(IAuthCommandService authCommandService, IAuthQueryService authQueryService) {
		this.authCommandService = authCommandService;
		this.authQueryService = authQueryService;
	}

	@PostMapping("/login")
	public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return Result.success(authCommandService.login(request));
	}

	@GetMapping("/info")
	public Result<AdminInfoResponse> getAdminInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		return Result.success(authQueryService.getAdminInfo(authorizationHeader));
	}
}