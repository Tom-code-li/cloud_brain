package com.cloudBrain.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundItemRequest {
    @NotNull(message = "药品ID不能为空")
    private Long drugId;
    @NotNull(message = "退药数量不能为空")
    private BigDecimal refundQuantity;
    private String drugName;
    private String specification;
    private String unit;
}
