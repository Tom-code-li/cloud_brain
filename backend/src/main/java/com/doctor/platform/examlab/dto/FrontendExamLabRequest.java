package com.doctor.platform.examlab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FrontendExamLabRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;

    private Long recordId;
    private List<ExamLabRequestItem> examItems;
    private List<ExamLabRequestItem> labItems;
    private ExamLabRequestForm form;
}
