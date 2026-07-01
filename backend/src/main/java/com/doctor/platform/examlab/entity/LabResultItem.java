package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lab_result_item")
public class LabResultItem {

    @TableId(type = IdType.AUTO)
    private Long resultItemId;
    private Long reportId;
    private Long orderItemId;
    private String itemCode;
    private String indicatorCode;
    private String indicatorName;
    private String resultValue;
    private String unit;
    private String referenceRange;
    private String abnormalFlag;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
