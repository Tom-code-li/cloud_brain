package com.hospital.registration.domain;

import java.math.BigDecimal;

public record FeeOrderView(
        Long feeOrderId,
        Long businessId,
        Long patientId,
        String patientName,
        Long registrationId,
        String feeType,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal refundAmount,
        String payStatus,
        boolean executed,
        String businessStatus
) {
}
