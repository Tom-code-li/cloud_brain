package com.doctor.platform.examlab.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.examlab.dto.ExamLabOrderDetailResponse;
import com.doctor.platform.examlab.dto.ExamLabOrderSaveRequest;
import com.doctor.platform.examlab.service.ExamLabOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-lab-orders")
public class ExamLabOrderController {

    private final ExamLabOrderService examLabOrderService;

    public ExamLabOrderController(ExamLabOrderService examLabOrderService) {
        this.examLabOrderService = examLabOrderService;
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> saveOrder(@Valid @RequestBody ExamLabOrderSaveRequest request) {
        Long orderId = examLabOrderService.saveOrder(request);
        return ApiResponse.success(Map.of("orderId", orderId));
    }

    @GetMapping
    public ApiResponse<List<ExamLabOrderDetailResponse>> listOrders(
        @RequestParam Long visitId,
        @RequestParam(required = false) String orderType
    ) {
        return ApiResponse.success(examLabOrderService.listOrdersByVisit(visitId, orderType));
    }
}
