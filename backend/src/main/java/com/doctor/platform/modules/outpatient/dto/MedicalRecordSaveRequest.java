package com.doctor.platform.modules.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordSaveRequest {

    private Long recordId;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;

    private String chiefComplaint;
    private String presentIllness;
    private String currentTreatment;
    private String pastHistory;
    private String allergyHistory;
    private String physicalExam;
    private String auxiliaryExam;
    private String diagnosis;
    private String treatmentAdvice;
    private String doctorNote;
    private String status;
}
