package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalRecordResponse {

    private Long recordId;
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
    private String finalDiagnosis;
    private String finalOpinion;
    private String status;
}
