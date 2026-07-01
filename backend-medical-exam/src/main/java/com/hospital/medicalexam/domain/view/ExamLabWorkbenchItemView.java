package com.hospital.medicalexam.domain.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExamLabWorkbenchItemView(
        Long orderId,
        Long orderItemId,
        Long visitId,
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
        Long reportId,
        String reportStatus,
        LocalDateTime publishedAt,
        String workbenchStatus
) {
}
