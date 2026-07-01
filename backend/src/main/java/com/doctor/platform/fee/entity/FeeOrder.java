package com.doctor.platform.fee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_order")
public class FeeOrder {

    @TableId(type = IdType.AUTO)
    private Long feeOrderId;
    private String orderNo;
    private Long patientId;
    private Long registrationId;
    private Long visitId;
    private String businessType;
    private Long businessId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal refundAmount;
    private String status;
    private Long createdBy;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
