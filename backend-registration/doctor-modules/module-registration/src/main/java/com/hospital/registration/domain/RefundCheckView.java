package com.hospital.registration.domain;

public record RefundCheckView(
        Long feeOrderId,
        boolean refundable,
        String reason
) {
}
