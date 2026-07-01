package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamLabOrderSaveResult {

    private Long orderId;
    private Long feeOrderId;
}
