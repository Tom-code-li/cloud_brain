package com.cloudBrain.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispense_item")
public class DispenseItem {

    @TableId(type = IdType.AUTO)
    private Long itemId;

    @TableField("dispense_id")
    private Long dispenseId;

    @TableField("drug_id")
    private String drugId;

    @TableField("drug_name")
    private String drugName;

    @TableField("specification")
    private String specification;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit")
    private String unit;

    @TableField("`usage`")
    private String usage;

    @TableField(exist = false)
    private Integer stock;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}