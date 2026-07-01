package com.doctor.platform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiSuggestionRequest {

    @NotBlank(message = "场景编码不能为空")
    private String sceneCode;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    private Long visitId;
    private Long recordId;
    private Long orderId;
    private Long reportId;

    private String currentChiefComplaint;
    private String currentPresentIllness;
    private String currentPhysicalExam;
    private String currentDiagnosis;
}
