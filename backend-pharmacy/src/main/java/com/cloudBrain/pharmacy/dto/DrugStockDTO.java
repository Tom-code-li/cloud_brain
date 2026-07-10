package com.cloudBrain.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DrugStockDTO {
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String specification;
    private String unit;
    private BigDecimal stock;
    private BigDecimal warningQuantity;
    private BigDecimal salePrice;
    private String defaultSupplier;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
