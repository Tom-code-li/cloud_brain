package com.doctor.platform.examlab.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamLabRequestItem {

    private Long id;
    private Long itemId;
    private String code;
    private String name;
    private BigDecimal price;
    private BigDecimal quantity;
}
