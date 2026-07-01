package com.doctor.platform.triage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("triage_record")
public class TriageRecord {

    @TableId(type = IdType.AUTO)
    private Long triageId;
    private Long patientId;
    private Long consultationId;
    private Long registrationId;
    private Long triageDoctorId;
    private Long recommendedDeptId;
    private String chiefComplaint;
    private String riskLevel;
    private String triageResult;
    private String status;
    private LocalDateTime createdAt;
}
