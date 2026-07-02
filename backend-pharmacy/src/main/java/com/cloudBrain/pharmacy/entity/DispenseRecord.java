package com.cloudBrain.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dispense_record")
public class DispenseRecord {

    @TableId(type = IdType.AUTO)
    private Long dispenseId;

    @TableField("prescription_no")
    private String prescriptionNo;

    @TableField("patient_id")
    private Long patientId;

    @TableField("patient_name")
    private String patientName;

    @TableField("gender")
    private String gender;

    @TableField("age")
    private Integer age;

    @TableField("department")
    private String department;

    @TableField("doctor_name")
    private String doctorName;

    @TableField("diagnosis")
    private String diagnosis;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("pay_status")
    private String payStatus;

    @TableField("status")
    private String status;

    @TableField("pharmacist")
    private String pharmacist;

    @TableField("dispensed_at")
    private LocalDateTime dispensedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}