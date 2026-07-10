package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamResultFeatureResponse {

    private Long featureId;
    private String featureName;
    private String featureValue;
    private String unit;
    private String abnormalFlag;
    private Integer sortOrder;
}
