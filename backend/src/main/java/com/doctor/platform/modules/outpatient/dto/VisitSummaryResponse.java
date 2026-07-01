package com.doctor.platform.modules.outpatient.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VisitSummaryResponse {

    private Long visitId;
    private String visitNo;
    private Integer queueNo;
    private String visitStatus;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
