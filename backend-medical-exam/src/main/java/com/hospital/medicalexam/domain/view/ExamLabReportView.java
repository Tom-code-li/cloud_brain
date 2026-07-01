package com.hospital.medicalexam.domain.view;

import java.time.LocalDateTime;

public record ExamLabReportView(
        Long reportId,
        Long orderId,
        Long orderItemId,
        Long recordId,
        Long patientId,
        String patientName,
        String itemName,
        String reportType,
        String findings,
        String conclusion,
        String aiDraft,
        String doctorReview,
        String status,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
}
