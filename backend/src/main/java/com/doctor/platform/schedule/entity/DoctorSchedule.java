package com.doctor.platform.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("doctor_schedule")
public class DoctorSchedule {

    @TableId(type = IdType.AUTO)
    private Long scheduleId;
    private Long doctorId;
    private Long deptId;
    private LocalDate workDate;
    private String timePeriod;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalQuota;
    private Integer remainQuota;
    private BigDecimal registrationFee;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
