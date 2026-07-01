package com.doctor.platform.prescription.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long prescriptionId;
    private String prescriptionNo;
    private Long visitId;
    private Long recordId;
    private Long patientId;
    private Long doctorId;
    private BigDecimal totalAmount;
    private String feeStatus;
    private String auditStatus;
    private String status;
    private String diagnosis;
    private String usageNote;
    private Long auditDoctorId;
    private String auditNote;
    private LocalDateTime auditedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
