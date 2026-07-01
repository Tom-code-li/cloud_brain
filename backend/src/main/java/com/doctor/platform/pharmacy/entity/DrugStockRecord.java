package com.doctor.platform.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug_stock_record")
public class DrugStockRecord {

    @TableId(type = IdType.AUTO)
    private Long stockRecordId;
    private Long drugId;
    private String businessType;
    private Long businessId;
    private BigDecimal changeQuantity;
    private BigDecimal beforeQuantity;
    private BigDecimal afterQuantity;
    private Long operatorUserId;
    private String remark;
    private LocalDateTime createdAt;
}
