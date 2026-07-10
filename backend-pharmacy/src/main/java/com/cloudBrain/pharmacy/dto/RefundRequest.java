package com.cloudBrain.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RefundRequest {
    @NotNull(message = "发药单ID不能为空")
    private Long dispenseId;

    @NotEmpty(message = "退药明细不能为空")
    @Valid
    private List<RefundItemRequest> items;

    private String reason;
    private Long operatorUserId;
}
