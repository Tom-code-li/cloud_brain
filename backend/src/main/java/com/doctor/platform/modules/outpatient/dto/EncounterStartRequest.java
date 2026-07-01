package com.doctor.platform.modules.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EncounterStartRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;
}
