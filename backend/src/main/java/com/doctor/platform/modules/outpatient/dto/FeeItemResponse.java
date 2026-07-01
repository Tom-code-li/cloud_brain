package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeItemResponse {

    private Long feeOrderItemId;
    private String itemType;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemSpec;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String status;
}
