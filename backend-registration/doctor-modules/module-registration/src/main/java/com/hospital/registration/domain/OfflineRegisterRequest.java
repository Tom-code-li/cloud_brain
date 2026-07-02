package com.hospital.registration.domain;

public record OfflineRegisterRequest(Long patientId, Long doctorId, Long scheduleId) {
}
