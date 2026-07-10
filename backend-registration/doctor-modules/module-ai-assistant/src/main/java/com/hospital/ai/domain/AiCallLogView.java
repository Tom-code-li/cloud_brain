package com.hospital.ai.domain;

import java.time.LocalDateTime;

public record AiCallLogView(
        Long logId,
        String roleCode,
        String sceneCode,
        String businessType,
        Long businessId,
        Long doctorId,
        Long patientId,
        String inputText,
        String outputText,
        String modelName,
        boolean changedBusinessStatus,
        LocalDateTime createdAt
) {
}
