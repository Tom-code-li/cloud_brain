package com.hospital.ai.common;

import java.util.Map;

public record RoleAiRequest(
        String roleCode,
        String sceneCode,
        Long businessId,
        Long doctorId,
        Long patientId,
        Map<String, Object> contextData
) {
}
