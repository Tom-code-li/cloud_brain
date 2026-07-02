package com.hospital.common.security;

public record DoctorPrincipal(
        Long userId,
        Long doctorId,
        Long deptId,
        String doctorType,
        String roleCode,
        String realName
) {
}
