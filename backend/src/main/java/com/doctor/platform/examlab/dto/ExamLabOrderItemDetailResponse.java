package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExamLabOrderItemDetailResponse {

    private Long orderItemId;
    private Long itemId;
    private String itemName;
    private String itemType;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String status;
    private String resultSummary;
}
