package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.Prescription;
import com.neu.patient.entity.PrescriptionItem;
import com.neu.patient.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {
    @Autowired private PrescriptionService prescriptionService;
    @GetMapping("/my/{patientId}")
    public Result<List<Prescription>> myPrescriptions(@PathVariable Long patientId) {
        return Result.ok(prescriptionService.getMyPrescriptions(patientId));
    }
    @GetMapping("/detail/{prescriptionId}")
    public Result<Prescription> detail(@PathVariable Long prescriptionId) {
        return Result.ok(prescriptionService.getPrescriptionDetail(prescriptionId));
    }
    @GetMapping("/items/{prescriptionId}")
    public Result<List<PrescriptionItem>> items(@PathVariable Long prescriptionId) {
        return Result.ok(prescriptionService.getPrescriptionItems(prescriptionId));
    }
}
