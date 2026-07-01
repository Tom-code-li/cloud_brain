package com.doctor.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_call_log")
public class AiCallLog {

    @TableId(type = IdType.AUTO)
    private Long callId;
    private Long userId;
    private Long doctorId;
    private Long patientId;
    private String roleCode;
    private String sceneCode;
    private String businessType;
    private Long businessId;
    private String prompt;
    private String response;
    private String modelName;
    private String apiKeyRef;
    private String status;
    private Integer changedBusinessStatus;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
