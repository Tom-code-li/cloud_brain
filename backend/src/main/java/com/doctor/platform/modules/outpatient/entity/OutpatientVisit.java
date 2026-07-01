package com.doctor.platform.modules.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outpatient_visit")
public class OutpatientVisit {

    @TableId(type = IdType.AUTO)
    private Long visitId;
    private Long registrationId;
    private Long patientId;
    private Long doctorId;
    private Long deptId;
    private String visitNo;
    private Integer queueNo;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
