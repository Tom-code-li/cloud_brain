package com.cloudBrain.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundResponse {
    private Long refundId;
    private Long dispenseId;
    private String prescriptionNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundedAt;
}
