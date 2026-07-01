package com.hospital.medicalexam.domain.dto;

import java.util.Map;

public record LabResultSaveRequest(
        Long orderItemId,
        String itemName,
        Map<String, String> values
) {
}