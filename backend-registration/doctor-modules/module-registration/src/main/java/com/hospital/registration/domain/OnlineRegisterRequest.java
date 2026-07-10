package com.hospital.registration.domain;

public record OnlineRegisterRequest(Long patientId, Long doctorId, Long scheduleId, boolean paid) {
}
