package com.doctor.platform.examlab.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.examlab.dto.MedicalItemResponse;
import com.doctor.platform.examlab.service.MedicalItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicalItemController {

    private final MedicalItemService medicalItemService;

    public MedicalItemController(MedicalItemService medicalItemService) {
        this.medicalItemService = medicalItemService;
    }

    @GetMapping("/api/exam-items")
    public ApiResponse<List<MedicalItemResponse>> listExamItems(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(medicalItemService.listExamItems(keyword));
    }

    @GetMapping("/api/lab-items")
    public ApiResponse<List<MedicalItemResponse>> listLabItems(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(medicalItemService.listLabItems(keyword));
    }
}
