package com.doctor.platform.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamLabReportContext {

    private Long reportId;
    private Long orderId;
    private Long orderItemId;
    private String orderNo;
    private String orderType;
    private String itemName;
    private String itemType;
    private String resultSummary;
    private String reportNo;
    private String reportType;
    private String findings;
    private String conclusion;
    private String aiDraft;
    private String doctorReview;
    private String status;
    private LocalDateTime publishedAt;
}
