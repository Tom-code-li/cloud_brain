package com.hospital.ai.domain;

import java.util.Map;

public record AiRequest(String businessType, Long businessId, Map<String, Object> contextData) {
}
