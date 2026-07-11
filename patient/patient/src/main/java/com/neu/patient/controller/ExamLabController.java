package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.ExamLabOrder;
import com.neu.patient.entity.ExamLabReport;
import com.neu.patient.service.ExamLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamLabController {
    @Autowired private ExamLabService examLabService;
    @GetMapping("/orders/{patientId}")
    public Result<List<ExamLabOrder>> myOrders(@PathVariable Long patientId) {
        return Result.ok(examLabService.getMyOrders(patientId));
    }
    @GetMapping("/reports/{patientId}")
    public Result<List<ExamLabReport>> myReports(@PathVariable Long patientId) {
        return Result.ok(examLabService.getMyReports(patientId));
    }
    @GetMapping("/report/{reportId}")
    public Result<ExamLabReport> reportDetail(@PathVariable Long reportId) {
        return Result.ok(examLabService.getReportDetail(reportId));
    }
}
