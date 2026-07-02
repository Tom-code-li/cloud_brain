package com.cloudBrain.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequest {
    @NotNull(message = "药品ID不能为空")
    private Long drugId;
    @NotNull(message = "采购数量不能为空")
    private BigDecimal purchaseQuantity;
    private String supplier;
    private String remark;
}
