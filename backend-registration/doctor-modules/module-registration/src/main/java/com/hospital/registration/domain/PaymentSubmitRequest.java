package com.hospital.registration.domain;

public record PaymentSubmitRequest(Long registrationId, String payMethod) {
}
