package com.cloudBrain.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认发药请求
 */
@Data
public class MarkDispensedRequest {
    @NotNull(message = "发药ID不能为空")
    private Long dispenseId;
    private String pharmacist;
}