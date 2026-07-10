package com.hospital.medicalexam.domain.dto;

import java.util.Map;

public record ExamResultSaveRequest(
        Long orderItemId,
        String itemName,
        Map<String, Object> resultData
) {
}