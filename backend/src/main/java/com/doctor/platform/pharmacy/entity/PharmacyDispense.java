package com.doctor.platform.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pharmacy_dispense")
public class PharmacyDispense {

    @TableId(type = IdType.AUTO)
    private Long dispenseId;
    private Long prescriptionId;
    private Long patientId;
    private Long pharmacyDoctorId;
    private String dispenseNo;
    private BigDecimal totalAmount;
    private String status;
    private String auditNote;
    private LocalDateTime dispensedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
