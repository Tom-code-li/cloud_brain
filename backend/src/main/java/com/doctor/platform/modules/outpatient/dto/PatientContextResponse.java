package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientContextResponse {

    private Long patientId;
    private String patientNo;
    private String patientName;
    private String gender;
    private String idCard;
    private String phone;
    private RegistrationSummaryResponse registration;
    private VisitSummaryResponse visit;
    private MedicalRecordResponse medicalRecord;
    private List<ExamLabSummaryResponse> examOrders;
    private List<ExamLabSummaryResponse> labOrders;
    private List<PrescriptionSummaryResponse> prescriptions;
    private List<FeeSummaryResponse> feeOrders;
}
