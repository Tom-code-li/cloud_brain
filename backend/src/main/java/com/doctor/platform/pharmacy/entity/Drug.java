package com.doctor.platform.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long drugId;
    private String drugCode;
    private String drugName;
    private String specification;
    private String dosageForm;
    private String manufacturer;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal stockQuantity;
    private BigDecimal warningQuantity;
    private String contraindication;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
