package com.cloudBrain.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseResponse {
    private Long purchaseId;
    private Long drugId;
    private String drugName;
    private BigDecimal purchaseQuantity;
    private String supplier;
    private BigDecimal stockAfterPurchase;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
