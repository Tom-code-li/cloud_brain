package com.cloudBrain.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("drug_stock")
public class DrugStock {

    @TableId(type = IdType.INPUT)
    private String drugId;

    @TableField("drug_name")
    private String drugName;

    @TableField("specification")
    private String specification;

    @TableField("stock")
    private Integer stock;

    @TableField("unit")
    private String unit;

    @TableField("default_supplier")
    private String defaultSupplier;

    @TableField("low_threshold")
    private Integer lowThreshold;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}