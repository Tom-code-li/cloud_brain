package com.doctor.platform.fee.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeOrderItemCommand {

    private String itemType;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemSpec;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
}
