package com.hospital.registration.domain;

import java.time.LocalDate;

public record RegistrationView(
        Long registrationId,
        String registrationNo,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        String deptName,
        Long scheduleId,
        LocalDate workDate,
        String timePeriod,
        Integer queueNo,
        String feeStatus,
        String status
) {
}
