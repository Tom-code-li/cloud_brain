package com.hospital.medicalexam.ai;

public record AiDraftRequest(
        Long orderId,
        Long orderItemId,
        Long patientId,
        String reportType,
        String itemName,
        String patientName,
        String gender,
        String resultDetail
) {
}
