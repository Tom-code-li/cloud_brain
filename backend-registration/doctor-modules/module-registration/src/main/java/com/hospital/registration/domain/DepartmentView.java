package com.hospital.registration.domain;

public record DepartmentView(
        Long deptId,
        String deptCode,
        String deptName,
        String deptType,
        String location,
        String description
) {
}
