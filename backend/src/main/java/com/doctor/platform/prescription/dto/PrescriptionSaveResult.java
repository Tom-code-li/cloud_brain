package com.doctor.platform.prescription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrescriptionSaveResult {

    private Long prescriptionId;
    private Long feeOrderId;
}
