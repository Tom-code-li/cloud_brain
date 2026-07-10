package com.doctor.platform.examlab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportReviewRequest {

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "就诊ID不能为空")
    private Long visitId;

    @NotNull(message = "报告ID不能为空")
    private Long reportId;
}
