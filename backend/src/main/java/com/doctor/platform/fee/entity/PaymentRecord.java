package com.doctor.platform.fee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long paymentId;
    private Long feeOrderId;
    private String paymentNo;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private String payerName;
    private String status;
    private LocalDateTime paidAt;
    private Long operatorUserId;
    private String remark;
}
