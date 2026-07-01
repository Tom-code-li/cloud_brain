package com.doctor.platform.fee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_record")
public class RefundRecord {

    @TableId(type = IdType.AUTO)
    private Long refundId;
    private Long feeOrderId;
    private Long paymentId;
    private String refundNo;
    private String refundType;
    private BigDecimal refundAmount;
    private String reason;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private Long operatorUserId;
}
