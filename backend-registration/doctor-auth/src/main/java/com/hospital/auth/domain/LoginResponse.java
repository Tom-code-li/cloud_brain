package com.hospital.auth.domain;

public record LoginResponse(
        String token,
        Long userId,
        Long doctorId,
        Long deptId,
        String doctorType,
        String roleCode,
        String realName,
        String deptName
) {
}
