package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.MedicalRecord;
import com.neu.patient.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical-record")
public class MedicalRecordController {
    @Autowired private MedicalRecordService medicalRecordService;
    @GetMapping("/my/{patientId}")
    public Result<List<MedicalRecord>> myRecords(@PathVariable Long patientId) {
        return Result.ok(medicalRecordService.getMyRecords(patientId));
    }
    @GetMapping("/detail/{recordId}")
    public Result<MedicalRecord> detail(@PathVariable Long recordId) {
        return Result.ok(medicalRecordService.getRecordDetail(recordId));
    }
}
