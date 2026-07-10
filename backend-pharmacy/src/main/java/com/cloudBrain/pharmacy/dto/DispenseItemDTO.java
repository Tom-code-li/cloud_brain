package com.cloudBrain.pharmacy.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DispenseItemDTO {
    private Long drugId;
    private String drugName;
    private String specification;
    private BigDecimal quantity;
    private String unit;
    private String usage;
    private BigDecimal stock;
}
