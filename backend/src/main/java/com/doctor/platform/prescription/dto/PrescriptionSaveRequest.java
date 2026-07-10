package com.doctor.platform.prescription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PrescriptionSaveRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;

    private Long recordId;
    private String diagnosis;
    private String usageNote;
    private BigDecimal totalAmount;
    private List<PrescriptionItemRequest> items;
}
