package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExamLabOrderDetailResponse {

    private Long orderId;
    private String orderNo;
    private Long visitId;
    private String orderType;
    private String purpose;
    private String examSite;
    private String specimenType;
    private String remark;
    private String priority;
    private String collectionWay;
    private String status;
    private String feeStatus;
    private BigDecimal totalAmount;
    private LocalDateTime appliedAt;
    private List<ExamLabOrderItemDetailResponse> items;
}
