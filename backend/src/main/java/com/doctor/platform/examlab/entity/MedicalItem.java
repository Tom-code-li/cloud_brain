package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medical_item")
public class MedicalItem {

    @TableId(type = IdType.AUTO)
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemType;
    private Long deptId;
    private String unit;
    private BigDecimal price;
    private String sampleType;
    private String clinicalMeaning;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
