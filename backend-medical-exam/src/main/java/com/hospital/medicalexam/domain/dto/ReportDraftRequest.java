package com.hospital.medicalexam.domain.dto;

public record ReportDraftRequest(
        Long orderItemId,
        String resultDetail,
        String aiReportContent
) {
}
