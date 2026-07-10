package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MedicalItemResponse {

    private Long id;
    private String code;
    private String name;
    private String type;
    private String spec;
    private BigDecimal price;
    private String feeType;
}
