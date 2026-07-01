package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationSummaryResponse {

    private Long registrationId;
    private String registrationNo;
    private Integer queueNo;
    private String feeStatus;
    private String status;
    private LocalDateTime registeredAt;
}
