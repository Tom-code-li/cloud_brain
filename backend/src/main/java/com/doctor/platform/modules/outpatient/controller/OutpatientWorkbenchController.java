package com.doctor.platform.modules.outpatient.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.modules.outpatient.dto.EncounterStartRequest;
import com.doctor.platform.modules.outpatient.dto.FinalDiagnosisSaveRequest;
import com.doctor.platform.modules.outpatient.dto.MedicalRecordSaveRequest;
import com.doctor.platform.modules.outpatient.dto.PatientContextResponse;
import com.doctor.platform.modules.outpatient.dto.PatientListItemResponse;
import com.doctor.platform.modules.outpatient.dto.SkipRequest;
import com.doctor.platform.modules.outpatient.service.OutpatientWorkbenchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/outpatient")
public class OutpatientWorkbenchController {

    private final OutpatientWorkbenchService outpatientWorkbenchService;

    public OutpatientWorkbenchController(OutpatientWorkbenchService outpatientWorkbenchService) {
        this.outpatientWorkbenchService = outpatientWorkbenchService;
    }

    @GetMapping("/patients")
    public ApiResponse<List<PatientListItemResponse>> listPatients(
        @RequestParam(required = false) String patientNo,
        @RequestParam(required = false) String patientName,
        @RequestParam(required = false) String visitStatus,
        @RequestParam(required = false) String visitGroup
    ) {
        return ApiResponse.success(outpatientWorkbenchService.listPatients(patientNo, patientName, visitStatus, visitGroup));
    }

    @GetMapping("/patients/{patientId}/context")
    public ApiResponse<PatientContextResponse> getPatientContext(@PathVariable Long patientId) {
        return ApiResponse.success(outpatientWorkbenchService.getPatientContext(patientId));
    }

    @PostMapping("/medical-records")
    public ApiResponse<Map<String, Long>> saveMedicalRecord(@Valid @RequestBody MedicalRecordSaveRequest request) {
        Long recordId = outpatientWorkbenchService.saveMedicalRecord(request);
        return ApiResponse.success(Map.of("recordId", recordId));
    }

    @PostMapping("/final-diagnosis")
    public ApiResponse<Map<String, Long>> saveFinalDiagnosis(@Valid @RequestBody FinalDiagnosisSaveRequest request) {
        Long recordId = outpatientWorkbenchService.saveFinalDiagnosis(request);
        return ApiResponse.success(Map.of("recordId", recordId));
    }

    @PostMapping("/skip-exam")
    public ApiResponse<Map<String, Long>> skipExam(@Valid @RequestBody SkipRequest request) {
        Long recordId = outpatientWorkbenchService.skipExam(request);
        return ApiResponse.success(Map.of("recordId", recordId));
    }

    @PostMapping("/skip-lab")
    public ApiResponse<Map<String, Long>> skipLab(@Valid @RequestBody SkipRequest request) {
        Long recordId = outpatientWorkbenchService.skipLab(request);
        return ApiResponse.success(Map.of("recordId", recordId));
    }

    @PostMapping("/start-encounter")
    public ApiResponse<Map<String, Long>> startEncounter(@Valid @RequestBody EncounterStartRequest request) {
        Long visitId = outpatientWorkbenchService.startEncounter(request);
        return ApiResponse.success(Map.of("visitId", visitId));
    }
}
