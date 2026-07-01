package com.doctor.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_schedule_suggestion")
public class AiScheduleSuggestion {

    @TableId(type = IdType.AUTO)
    private Long suggestionId;
    private Long doctorId;
    private Long deptId;
    private LocalDate workDate;
    private String timePeriod;
    private Integer suggestedQuota;
    private String suggestionReason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}
