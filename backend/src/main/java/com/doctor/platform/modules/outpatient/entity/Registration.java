package com.doctor.platform.modules.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long registrationId;
    private Long patientId;
    private Long consultationId;
    private Long deptId;
    private Long doctorId;
    private Long scheduleId;
    private Long operatorUserId;
    private String source;
    private String registrationNo;
    private Integer queueNo;
    private BigDecimal registrationFee;
    private String feeStatus;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime calledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
