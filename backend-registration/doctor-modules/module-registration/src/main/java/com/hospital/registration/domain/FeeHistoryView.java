package com.hospital.registration.domain;

import java.util.List;

public record FeeHistoryView(
        Long patientId,
        Long registrationId,
        List<FeeOrderView> orders
) {
}
