package com.cloudBrain.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DispenseRecordDTO {
    private Long dispenseId;
    private String prescriptionNo;
    private Long patientId;
    private String patientName;
    private String gender;
    private Integer age;
    private String department;
    private String doctorName;
    private String diagnosis;
    private BigDecimal totalAmount;
    private String payStatus;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispensedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundedAt;

    private List<DispenseItemDTO> items;
}
