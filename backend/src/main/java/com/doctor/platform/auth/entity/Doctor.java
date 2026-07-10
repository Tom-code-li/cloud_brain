package com.doctor.platform.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.AUTO)
    private Long doctorId;
    private Long userId;
    private Long deptId;
    private String doctorNo;
    private String doctorType;
    private String title;
    private String specialty;
    private String introduction;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
