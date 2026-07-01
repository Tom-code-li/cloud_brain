package com.hospital.medicalexam.domain.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OrderDetailView(
        Long orderId,
        Long orderItemId,
        Long recordId,
        Long patientId,
        String patientName,
        String gender,
        LocalDate birthday,
        String applyDoctorName,
        String executeDeptName,
        String itemName,
        String itemType,
        BigDecimal amount,
        String feeStatus,
        String orderStatus,
        String itemStatus,
        String clinicalDiagnosis,
        String purpose,
        LocalDateTime appliedAt,
        LocalDateTime executedAt,
        String sampleId,
        Long reportId,
        String reportNo,
        String reportStatus,
        String findings,
        String conclusion,
        String aiDraft,
        String doctorReview,
        LocalDateTime publishedAt,
        List<Map<String, Object>> resultFeatures,
        List<Map<String, Object>> labResultItems
) {
}