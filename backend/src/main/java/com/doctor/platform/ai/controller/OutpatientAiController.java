package com.doctor.platform.ai.controller;

import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.ai.service.OutpatientAiService;
import com.doctor.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/outpatient")
public class OutpatientAiController {

    private final OutpatientAiService outpatientAiService;

    public OutpatientAiController(OutpatientAiService outpatientAiService) {
        this.outpatientAiService = outpatientAiService;
    }

    @PostMapping("/suggestions")
    public ApiResponse<AiDraftBlock> generateSuggestion(@Valid @RequestBody AiSuggestionRequest request) {
        return ApiResponse.success(outpatientAiService.generateSuggestion(request));
    }
}
