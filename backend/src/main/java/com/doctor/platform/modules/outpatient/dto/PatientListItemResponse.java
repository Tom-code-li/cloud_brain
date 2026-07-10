package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientListItemResponse {

    private Long patientId;
    private Long visitId;
    private String patientNo;
    private String patientName;
    private String gender;
    private String idCard;
    private Integer age;
    private String status;
    private String visitStatus;
    private Integer queueNo;
    private java.time.LocalDateTime registeredAt;
}
