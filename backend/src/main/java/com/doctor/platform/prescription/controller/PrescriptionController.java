package com.doctor.platform.prescription.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.prescription.dto.PrescriptionSaveResult;
import com.doctor.platform.prescription.dto.PrescriptionSaveRequest;
import com.doctor.platform.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ApiResponse<PrescriptionSaveResult> savePrescription(@Valid @RequestBody PrescriptionSaveRequest request) {
        return ApiResponse.success(prescriptionService.savePrescription(request));
    }
}
