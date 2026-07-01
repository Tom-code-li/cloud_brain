package com.hospital.medicalexam.domain.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExamLabTaskView(
        Long orderId,
        Long orderItemId,
        Long visitId,
        Long recordId,
        Long patientId,
        String patientName,
        String gender,
        String itemName,
        String itemType,
        BigDecimal amount,
        String feeStatus,
        String orderStatus,
        String itemStatus,
        String clinicalDiagnosis,
        String purpose,
        LocalDateTime appliedAt,
        LocalDateTime executedAt
) {
}
