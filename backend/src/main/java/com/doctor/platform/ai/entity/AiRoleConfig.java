package com.doctor.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_role_config")
public class AiRoleConfig {

    @TableId(type = IdType.AUTO)
    private Long configId;
    private String roleCode;
    private String roleName;
    private String provider;
    private String modelName;
    private String apiKeyRef;
    private Integer enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
