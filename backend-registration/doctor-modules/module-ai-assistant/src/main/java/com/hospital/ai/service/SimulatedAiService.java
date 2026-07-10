package com.hospital.ai.service;

import com.hospital.ai.common.RoleAiAssistant;
import com.hospital.ai.common.RoleAiAssistantService;
import com.hospital.ai.common.RoleAiConfigRegistry;
import com.hospital.ai.common.RoleAiRequest;
import com.hospital.ai.domain.AiCallLogView;
import com.hospital.ai.exam.ExamAiAssistant;
import com.hospital.ai.lab.LabAiAssistant;
import com.hospital.ai.outpatient.OutpatientAiAssistant;
import com.hospital.ai.pharmacy.PharmacyAiAssistant;
import com.hospital.ai.registration.RegistrationAiAssistant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SimulatedAiService {
    private final RoleAiAssistantService roleAiAssistantService;

    public SimulatedAiService(RoleAiAssistantService roleAiAssistantService) {
        this.roleAiAssistantService = roleAiAssistantService;
    }

    public SimulatedAiService() {
        RoleAiConfigRegistry registry = RoleAiConfigRegistry.defaults();
        List<RoleAiAssistant> assistants = List.of(
                new RegistrationAiAssistant(registry),
                new OutpatientAiAssistant(registry),
                new ExamAiAssistant(registry),
                new LabAiAssistant(registry),
                new PharmacyAiAssistant(registry)
        );
        this.roleAiAssistantService = new RoleAiAssistantService(registry, assistants);
    }

    public List<String> generateChunks(String businessType, String context) {
        return roleAiAssistantService.assistLegacy(
                businessType,
                null,
                Map.of("query", context == null ? "" : context),
                null
        );
    }

    public synchronized List<String> assist(String businessType, Long businessId, Map<String, Object> contextData, Long doctorId) {
        return roleAiAssistantService.assistLegacy(businessType, businessId, contextData, doctorId);
    }

    public synchronized List<String> assist(RoleAiRequest request) {
        return roleAiAssistantService.assist(request);
    }

    public synchronized List<AiCallLogView> logs() {
        return roleAiAssistantService.logs();
    }
}
