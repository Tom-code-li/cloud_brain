package com.doctor.platform.fee.controller;

import com.doctor.platform.fee.dto.FeeOrderItemResponse;
import com.doctor.platform.fee.service.FeeOrderService;
import com.doctor.platform.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fee-orders")
public class FeeOrderController {

    private final FeeOrderService feeOrderService;

    public FeeOrderController(FeeOrderService feeOrderService) {
        this.feeOrderService = feeOrderService;
    }

    @GetMapping
    public ApiResponse<List<FeeOrderItemResponse>> listFeeOrders(@RequestParam Long visitId) {
        return ApiResponse.success(feeOrderService.listFeeOrdersByVisit(visitId));
    }
}
