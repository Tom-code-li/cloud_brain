package com.doctor.platform.examlab.dto;

import lombok.Data;

@Data
public class ExamLabRequestForm {

    private String purpose;
    private String site;
    private String specimen;
    private String notes;
    private String priority;
    private String collectionWay;
}
