package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_lab_order")
public class ExamLabOrder {

    @TableId(type = IdType.AUTO)
    private Long orderId;
    private String orderNo;
    private Long visitId;
    private Long recordId;
    private Long patientId;
    private Long applyDoctorId;
    private Long executeDeptId;
    private String orderType;
    private String clinicalDiagnosis;
    private String purpose;
    private String examSite;
    private String specimenType;
    private String remark;
    private String priority;
    private String collectionWay;
    private BigDecimal totalAmount;
    private String feeStatus;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime executedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
