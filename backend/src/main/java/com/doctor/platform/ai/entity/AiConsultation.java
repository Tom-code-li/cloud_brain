package com.doctor.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_consultation")
public class AiConsultation {

    @TableId(type = IdType.AUTO)
    private Long consultationId;
    private Long patientId;
    private String chiefComplaint;
    private String symptomDetail;
    private String aiSummary;
    private Long recommendedDeptId;
    private String riskLevel;
    private String aiResult;
    private String status;
    private LocalDateTime createdAt;
}
