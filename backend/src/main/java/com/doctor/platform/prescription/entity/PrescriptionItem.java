package com.doctor.platform.prescription.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prescription_item")
public class PrescriptionItem {

    @TableId(type = IdType.AUTO)
    private Long prescriptionItemId;
    private Long prescriptionId;
    private Long drugId;
    private String drugName;
    private String specification;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String dosage;
    private String frequency;
    private String usageMethod;
    private Integer days;
    private String status;
    private LocalDateTime createdAt;
}
