package com.doctor.platform.examlab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_lab_report")
public class ExamLabReport {

    @TableId(type = IdType.AUTO)
    private Long reportId;
    private Long orderId;
    private Long orderItemId;
    private Long patientId;
    private Long reportDoctorId;
    private String reportNo;
    private String reportType;
    private String findings;
    private String conclusion;
    private String aiDraft;
    private String doctorReview;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
