package com.doctor.platform.pharmacy.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DrugResponse {

    private Long drugId;
    private String code;
    private String name;
    private String spec;
    private BigDecimal price;
    private String factory;
}
