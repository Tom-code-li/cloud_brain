package com.doctor.platform.examlab.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.examlab.dto.ExamLabOrderSaveResult;
import com.doctor.platform.examlab.dto.FrontendExamLabRequest;
import com.doctor.platform.examlab.service.ExamLabOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FrontendExamLabRequestController {

    private final ExamLabOrderService examLabOrderService;

    public FrontendExamLabRequestController(ExamLabOrderService examLabOrderService) {
        this.examLabOrderService = examLabOrderService;
    }

    @PostMapping("/api/exam-request")
    public ApiResponse<ExamLabOrderSaveResult> saveExamRequest(@Valid @RequestBody FrontendExamLabRequest request) {
        return ApiResponse.success(examLabOrderService.saveFrontendRequest(request, "检查"));
    }

    @PostMapping("/api/lab-request")
    public ApiResponse<ExamLabOrderSaveResult> saveLabRequest(@Valid @RequestBody FrontendExamLabRequest request) {
        return ApiResponse.success(examLabOrderService.saveFrontendRequest(request, "检验"));
    }
}
