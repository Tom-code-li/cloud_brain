package com.doctor.platform.fee.dto;

import com.doctor.platform.modules.outpatient.dto.FeeItemResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeeOrderItemResponse {

    private Long feeOrderId;
    private String orderNo;
    private Long visitId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<FeeItemResponse> items;
}
