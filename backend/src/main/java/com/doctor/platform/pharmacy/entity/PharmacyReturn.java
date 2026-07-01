package com.doctor.platform.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pharmacy_return")
public class PharmacyReturn {

    @TableId(type = IdType.AUTO)
    private Long returnId;
    private Long dispenseId;
    private Long prescriptionId;
    private Long drugId;
    private String returnNo;
    private BigDecimal returnQuantity;
    private BigDecimal returnAmount;
    private String reason;
    private String status;
    private Long operatorUserId;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;
}
