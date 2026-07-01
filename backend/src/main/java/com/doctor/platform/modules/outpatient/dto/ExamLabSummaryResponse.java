package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExamLabSummaryResponse {

    private Long orderId;
    private String orderNo;
    private String orderType;
    private String purpose;
    private String status;
    private BigDecimal totalAmount;
}
