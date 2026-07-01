package com.doctor.platform.fee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_order_item")
public class FeeOrderItem {

    @TableId(type = IdType.AUTO)
    private Long feeOrderItemId;
    private Long feeOrderId;
    private String itemType;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemSpec;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
