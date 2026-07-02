package com.cloudBrain.pharmacy.dto;

import lombok.Data;

@Data
public class DispenseQuery {
    private Long patientId;
    private String keyword;
    private String department;
    private String status;
}
