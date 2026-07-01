package com.doctor.platform.examlab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamLabOrderSaveRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;

    private Long recordId;

    @NotBlank(message = "医嘱类型不能为空")
    private String orderType;

    private String clinicalDiagnosis;
    private String purpose;
    private BigDecimal totalAmount;
}
