package com.doctor.platform.examlab.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabResultItemResponse {

    private Long resultItemId;
    private String itemCode;
    private String indicatorCode;
    private String indicatorName;
    private String resultValue;
    private String unit;
    private String referenceRange;
    private String abnormalFlag;
    private Integer sortOrder;
}
