package com.hospital.registration.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorScheduleView(
        Long scheduleId,
        Long doctorId,
        String doctorName,
        Long deptId,
        String deptName,
        LocalDate workDate,
        String timePeriod,
        LocalTime startTime,
        LocalTime endTime,
        int totalQuota,
        int remainQuota,
        BigDecimal registrationFee,
        String status
) {
}
