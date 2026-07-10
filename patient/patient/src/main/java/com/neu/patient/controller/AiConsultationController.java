package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.dto.AiConsultRequest;
import com.neu.patient.entity.AiConsultation;
import com.neu.patient.service.AiConsultationService;
import com.neu.patient.service.impl.AiConsultationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiConsultationController {
    @Autowired private AiConsultationService aiConsultationService;

    @PostMapping("/consult")
    public Result<AiConsultation> consult(@RequestBody AiConsultation consultation) {
        return Result.ok("问诊提交成功", aiConsultationService.createConsultation(consultation));
    }

    @PostMapping("/smart")
    public Result<AiConsultation> smartConsult(@RequestBody AiConsultRequest request) {
        if (!(aiConsultationService instanceof AiConsultationServiceImpl impl)) {
            return Result.fail("AI 服务未启用");
        }
        try {
            AiConsultation result = impl.handleConsultation(request);
            return Result.ok("问诊提交成功", result);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/my/{patientId}")
    public Result<List<AiConsultation>> myConsultations(@PathVariable Long patientId) {
        return Result.ok(aiConsultationService.getMyConsultations(patientId));
    }
}
