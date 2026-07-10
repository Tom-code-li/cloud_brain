package com.hospital.ai.common;

import com.hospital.ai.domain.AiCallLogView;
import com.hospital.common.core.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class RoleAiAssistantService {
    private final RoleAiConfigRegistry configRegistry;
    private final Map<String, RoleAiAssistant> assistants;
    private final AtomicLong logIdGenerator = new AtomicLong(1);
    private final List<AiCallLogView> logs = new java.util.ArrayList<>();

    public RoleAiAssistantService(
            RoleAiConfigRegistry configRegistry,
            List<RoleAiAssistant> assistants
    ) {
        this.configRegistry = configRegistry;
        this.assistants = assistants.stream()
                .collect(Collectors.toMap(
                        assistant -> RoleAiConfigRegistry.normalize(assistant.roleCode()),
                        assistant -> assistant,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    public synchronized List<String> assist(RoleAiRequest request) {
        RoleAiRequest normalizedRequest = normalizeRequest(request);
        RoleAiAssistant assistant = assistants.get(normalizedRequest.roleCode());
        if (assistant == null) {
            throw new BusinessException("未找到 AI 角色处理器：" + normalizedRequest.roleCode());
        }
        if (!assistant.supportedScenes().contains(normalizedRequest.sceneCode())) {
            throw new BusinessException("AI 场景不属于当前角色：" + normalizedRequest.roleCode() + "." + normalizedRequest.sceneCode());
        }

        RoleAiConfig config = configRegistry.roleConfig(normalizedRequest.roleCode());
        List<String> chunks = assistant.assist(normalizedRequest);
        logs.add(new AiCallLogView(
                logIdGenerator.getAndIncrement(),
                normalizedRequest.roleCode(),
                normalizedRequest.sceneCode(),
                normalizedRequest.roleCode() + "." + normalizedRequest.sceneCode(),
                normalizedRequest.businessId(),
                normalizedRequest.doctorId(),
                normalizedRequest.patientId(),
                String.valueOf(normalizedRequest.contextData() == null ? Map.of() : normalizedRequest.contextData()),
                String.join("", chunks),
                config.modelName(),
                false,
                LocalDateTime.now()
        ));
        return chunks;
    }

    public synchronized List<String> assistLegacy(
            String businessType,
            Long businessId,
            Map<String, Object> contextData,
            Long doctorId
    ) {
        RoleScene roleScene = mapLegacyBusinessType(businessType);
        return assist(new RoleAiRequest(
                roleScene.roleCode(),
                roleScene.sceneCode(),
                businessId,
                doctorId,
                extractLong(contextData, "patientId"),
                contextData
        ));
    }

    public synchronized List<AiCallLogView> logs() {
        return List.copyOf(logs);
    }

    private RoleAiRequest normalizeRequest(RoleAiRequest request) {
        if (request == null) {
            throw new BusinessException("AI 请求不能为空");
        }
        String roleCode = RoleAiConfigRegistry.normalize(request.roleCode());
        String sceneCode = RoleAiConfigRegistry.normalize(request.sceneCode());
        if (roleCode.isBlank()) {
            throw new BusinessException("AI 角色不能为空");
        }
        if (sceneCode.isBlank()) {
            throw new BusinessException("AI 场景不能为空");
        }
        return new RoleAiRequest(
                roleCode,
                sceneCode,
                request.businessId(),
                request.doctorId(),
                request.patientId(),
                request.contextData() == null ? Map.of() : request.contextData()
        );
    }

    private RoleScene mapLegacyBusinessType(String businessType) {
        String normalizedType = RoleAiConfigRegistry.normalize(businessType);
        return switch (normalizedType) {
            case "TRIAGE" -> new RoleScene("REGISTRATION", "TRIAGE");
            case "DEPARTMENT_RECOMMEND" -> new RoleScene("REGISTRATION", "DEPARTMENT_RECOMMEND");
            case "DOCTOR_RECOMMEND" -> new RoleScene("REGISTRATION", "DOCTOR_RECOMMEND");
            case "PATIENT_SUMMARY" -> new RoleScene("OUTPATIENT", "PATIENT_SUMMARY");
            case "DIAGNOSIS" -> new RoleScene("OUTPATIENT", "DIAGNOSIS");
            case "EXAM_SUGGESTION" -> new RoleScene("OUTPATIENT", "EXAM_SUGGESTION");
            case "REPORT_INTERPRET" -> new RoleScene("OUTPATIENT", "REPORT_INTERPRET");
            case "PRESCRIPTION" -> new RoleScene("OUTPATIENT", "PRESCRIPTION");
            case "MEDICAL_RECORD_GENERATE" -> new RoleScene("OUTPATIENT", "MEDICAL_RECORD_GENERATE");
            case "REPORT_DRAFT", "EXAM_REPORT_DRAFT" -> new RoleScene("EXAM", "REPORT_DRAFT");
            case "EXAM_REPORT_OPTIMIZE" -> new RoleScene("EXAM", "REPORT_OPTIMIZE");
            case "LAB_RESULT_INTERPRET" -> new RoleScene("LAB", "RESULT_INTERPRET");
            case "LAB_REPORT_CONCLUSION" -> new RoleScene("LAB", "REPORT_CONCLUSION");
            case "PRESCRIPTION_SUMMARY" -> new RoleScene("PHARMACY", "PRESCRIPTION_SUMMARY");
            case "MEDICATION_REVIEW" -> new RoleScene("PHARMACY", "MEDICATION_REVIEW");
            default -> new RoleScene("OUTPATIENT", "DIAGNOSIS");
        };
    }

    private Long extractLong(Map<String, Object> contextData, String key) {
        if (contextData == null || !contextData.containsKey(key)) {
            return null;
        }
        Object value = contextData.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record RoleScene(String roleCode, String sceneCode) {
    }
}
