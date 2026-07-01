package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FeeSummaryResponse {

    private Long feeOrderId;
    private String orderNo;
    private Long visitId;
    private String businessType;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
