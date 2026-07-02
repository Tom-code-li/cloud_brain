package com.cloudBrain.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockUpdateRequest {
    @NotNull(message = "库存不能为空")
    private BigDecimal stock;
    private BigDecimal purchaseQuantity;
    private String supplier;
    private String remark;
}
