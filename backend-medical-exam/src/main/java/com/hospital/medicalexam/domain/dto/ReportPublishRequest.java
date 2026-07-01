package com.hospital.medicalexam.domain.dto;

public record ReportPublishRequest(
        Long reportId,
        String doctorConclusion
) {
}
