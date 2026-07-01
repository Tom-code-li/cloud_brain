package com.hospital.medicalexam.controller;

import com.hospital.medicalexam.common.R;
import com.hospital.medicalexam.domain.dto.ExamResultSaveRequest;
import com.hospital.medicalexam.domain.dto.LabResultSaveRequest;
import com.hospital.medicalexam.domain.dto.OrderExecuteRequest;
import com.hospital.medicalexam.domain.dto.ReportDraftRequest;
import com.hospital.medicalexam.domain.dto.ReportPublishRequest;
import com.hospital.medicalexam.domain.dto.ReportRejectRequest;
import com.hospital.medicalexam.domain.dto.SampleConfirmRequest;
import com.hospital.medicalexam.domain.view.ExamLabReportView;
import com.hospital.medicalexam.domain.view.ExamLabTaskView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchResponse;
import com.hospital.medicalexam.domain.view.ItemSchemaView;
import com.hospital.medicalexam.domain.view.OrderDetailView;
import com.hospital.medicalexam.service.MedicalExamWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medical-exam")
public class MedicalExamController {
    private final MedicalExamWorkflowService workflowService;

    public MedicalExamController(MedicalExamWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/order/pending")
    public R<List<ExamLabTaskView>> pending(
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType
    ) {
        return R.ok(workflowService.pending(deptId, doctorType));
    }

    @GetMapping("/workbench")
    public R<ExamLabWorkbenchResponse> workbench(
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "patientName", required = false) String patientName,
            @RequestParam(value = "itemName", required = false) String itemName
    ) {
        return R.ok(workflowService.workbench(deptId, doctorType, status, resolveSearchKeyword(keyword, patientName), itemName));
    }

    private String resolveSearchKeyword(String keyword, String patientName) {
        if (keyword != null && !keyword.isBlank()) {
            return keyword;
        }
        return patientName;
    }

    @PostMapping("/order/execute")
    public R<ExamLabTaskView> executeOrder(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody OrderExecuteRequest request
    ) {
        return R.ok(workflowService.executeOrder(request.orderItemId(), doctorId, deptId, doctorType));
    }

    @PostMapping("/report/draft")
    public R<ExamLabReportView> createDraft(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody ReportDraftRequest request
    ) {
        return R.ok(workflowService.createDraft(request, doctorId, deptId, doctorType));
    }

    @PutMapping("/report/publish")
    public R<ExamLabReportView> publish(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody ReportPublishRequest request
    ) {
        return R.ok(workflowService.publish(request, doctorId, deptId, doctorType));
    }

    @GetMapping("/report/detail")
    public R<ExamLabReportView> detail(@RequestParam("reportId") Long reportId) {
        return R.ok(workflowService.detail(reportId));
    }

    @GetMapping("/report/by-record")
    public R<List<ExamLabReportView>> reportsByRecord(@RequestParam("recordId") Long recordId) {
        return R.ok(workflowService.reportsByRecord(recordId));
    }

    /* ---------- 新增: 详情、样本、结果录入、退回 ---------- */

    @GetMapping("/order/item-detail")
    public R<OrderDetailView> itemDetail(@RequestParam("orderItemId") Long orderItemId) {
        return R.ok(workflowService.getOrderItemDetail(orderItemId));
    }

    @PostMapping("/order/sample")
    public R<Void> confirmSample(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody SampleConfirmRequest request
    ) {
        workflowService.confirmSample(request, doctorId, deptId, doctorType);
        return R.ok();
    }

    @PostMapping("/result/exam")
    public R<Void> saveExamResult(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody ExamResultSaveRequest request
    ) {
        workflowService.saveExamResult(request, doctorId, deptId, doctorType);
        return R.ok();
    }

    @PostMapping("/result/lab")
    public R<Void> saveLabResult(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody LabResultSaveRequest request
    ) {
        workflowService.saveLabResult(request, doctorId, deptId, doctorType);
        return R.ok();
    }

    @PostMapping("/report/reject")
    public R<Void> rejectReport(
            @RequestHeader("X-Doctor-Id") Long doctorId,
            @RequestHeader(value = "X-Dept-Id", required = false) Long deptId,
            @RequestHeader("X-Doctor-Type") String doctorType,
            @RequestBody ReportRejectRequest request
    ) {
        workflowService.rejectReport(request, doctorId, deptId, doctorType);
        return R.ok();
    }

    @GetMapping("/item/schema")
    public R<ItemSchemaView> itemSchema(@RequestParam("itemName") String itemName) {
        return R.ok(workflowService.getItemSchema(itemName));
    }
}
