package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_result_feature")
public class ExamResultFeature {

    @TableId(type = IdType.AUTO)
    private Long featureId;
    private Long reportId;
    private Long orderItemId;
    private String featureName;
    private String featureValue;
    private String unit;
    private String abnormalFlag;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
