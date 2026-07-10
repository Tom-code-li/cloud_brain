package com.hospital.ai.common;

public record RoleAiConfig(
        String roleCode,
        String roleName,
        String provider,
        String modelName,
        String apiKeyRef,
        boolean enabled
) {
}
