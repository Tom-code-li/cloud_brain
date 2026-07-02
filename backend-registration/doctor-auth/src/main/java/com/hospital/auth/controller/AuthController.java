package com.hospital.auth.controller;

import com.hospital.auth.domain.LoginRequest;
import com.hospital.auth.domain.LoginResponse;
import com.hospital.auth.service.AuthService;
import com.hospital.common.core.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest request) {
        return R.ok(authService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public R<LoginResponse> me(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader("X-Dept-Id") Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestHeader("X-Role-Code") String roleCode,
            @RequestHeader("X-Real-Name") String realName
    ) {
        return R.ok(new LoginResponse("", userId, doctorId, deptId, doctorType, roleCode, realName, ""));
    }
}
