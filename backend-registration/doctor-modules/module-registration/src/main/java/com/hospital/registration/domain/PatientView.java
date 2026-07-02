package com.hospital.registration.domain;

public record PatientView(
        Long patientId,
        String patientNo,
        String patientName,
        String gender,
        String idCard,
        String phone,
        String allergyHistory,
        String pastHistory
) {
}
