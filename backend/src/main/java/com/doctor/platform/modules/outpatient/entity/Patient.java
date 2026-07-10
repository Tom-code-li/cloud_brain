package com.doctor.platform.modules.outpatient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("patient")
public class Patient {

    @TableId(type = IdType.AUTO)
    private Long patientId;
    private Long userId;
    private String patientNo;
    private String patientName;
    private String gender;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String allergyHistory;
    private String pastHistory;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
