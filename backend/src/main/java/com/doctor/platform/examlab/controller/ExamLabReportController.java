package com.doctor.platform.examlab.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.examlab.dto.ExamLabReportResponse;
import com.doctor.platform.examlab.dto.ReportReviewRequest;
import com.doctor.platform.examlab.service.ExamLabReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-lab-reports")
public class ExamLabReportController {

    private final ExamLabReportService examLabReportService;

    public ExamLabReportController(ExamLabReportService examLabReportService) {
        this.examLabReportService = examLabReportService;
    }

    @GetMapping
    public ApiResponse<List<ExamLabReportResponse>> listReports(
        @RequestParam(required = false) Long patientId,
        @RequestParam(required = false) Long visitId,
        @RequestParam(required = false) Long orderId
    ) {
        return ApiResponse.success(examLabReportService.listReports(patientId, visitId, orderId));
    }

    @PostMapping("/review")
    public ApiResponse<Map<String, Long>> markReviewed(@Valid @RequestBody ReportReviewRequest request) {
        Long reportId = examLabReportService.markReportReviewed(request);
        return ApiResponse.success(Map.of("reportId", reportId));
    }
}
