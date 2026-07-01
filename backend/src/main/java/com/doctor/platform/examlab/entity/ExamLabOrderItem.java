package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_lab_order_item")
public class ExamLabOrderItem {

    @TableId(type = IdType.AUTO)
    private Long orderItemId;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private String itemType;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String status;
    private LocalDateTime executedAt;
    private String resultSummary;
    private LocalDateTime createdAt;
}
