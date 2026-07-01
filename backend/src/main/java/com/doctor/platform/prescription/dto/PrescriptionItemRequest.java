package com.doctor.platform.prescription.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrescriptionItemRequest {

    private Long drugId;
    private String code;
    private String name;
    private String spec;
    private BigDecimal price;
    private BigDecimal quantity;
    private Integer count;
    private String dosage;
    private String frequency;
    private String usageMethod;
    private String usage;
    private Integer days;
}
