package com.hospital.registration.domain;

public record DoctorView(
        Long doctorId,
        String doctorName,
        Long deptId,
        String deptName,
        String title,
        String specialty
) {
}
