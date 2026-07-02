package com.hospital.registration.domain;

public record PatientSyncRequest(
        String patientName,
        String gender,
        String idCard,
        String phone,
        String allergyHistory,
        String pastHistory
) {
}
