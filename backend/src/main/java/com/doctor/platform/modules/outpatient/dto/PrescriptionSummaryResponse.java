package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PrescriptionSummaryResponse {

    private Long prescriptionId;
    private String prescriptionNo;
    private String diagnosis;
    private String status;
    private BigDecimal totalAmount;
}
