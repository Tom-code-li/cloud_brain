package com.neu.patient.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.dto.AiConsultRequest;
import com.neu.patient.dto.AiConsultResponse;
import com.neu.patient.entity.AiConsultation;
import com.neu.patient.entity.Department;
import com.neu.patient.mapper.AiConsultationMapper;
import com.neu.patient.mapper.DepartmentMapper;
import com.neu.patient.service.AiConsultationService;
import com.neu.patient.service.AiKnowledgeService;
import com.neu.patient.service.DeepSeekAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AiConsultationServiceImpl implements AiConsultationService {
    @Autowired private AiConsultationMapper aiConsultationMapper;
    @Autowired private DepartmentMapper departmentMapper;
    @Autowired private AiKnowledgeService aiKnowledgeService;
    @Autowired private DeepSeekAiService deepSeekAiService;
    @Autowired private ObjectMapper objectMapper;

    @Override
    public AiConsultation createConsultation(AiConsultation consultation) {
        consultation.setStatus(EnumValues.AI_GENERATED);
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setRiskLevel(EnumValues.RISK_NORMAL);
        aiConsultationMapper.insert(consultation);
        return consultation;
    }

    public AiConsultation handleConsultation(AiConsultRequest request) {
        List<Department> departments = departmentMapper.findAllActive();
        List<String> candidates = aiKnowledgeService.buildCandidates(request.getPatientId(), request.getMode(), request.getContent());
        String systemPrompt = """
                你是医院导诊助手。只能基于给定候选项回答，不得编造数据库不存在的科室、医生、病历、检查、处方、费用信息。
                输出必须是 JSON，字段包含：summary, riskLevel, recommendedDept, recommendedDoctor, aiResult, note。
                riskLevel 只能返回“普通”或“紧急”。
                recommendedDept 必须使用候选科室中的完整科室名称；如果候选科室里没有合适内容，recommendedDept 置空，note 说明未找到匹配项。
                """;
        String userPrompt = """
                用户模式：%s
                用户输入：%s
                可推荐科室：
                %s
                候选项：
                %s
                请只从候选项中选择，并输出严格 JSON。
                """.formatted(
                request.getMode(),
                request.getContent(),
                departments.stream()
                        .map(d -> "科室:" + safe(d.getDeptName()) + "，简介:" + safe(d.getDescription()))
                        .reduce((left, right) -> left + "\n" + right)
                        .orElse(""),
                String.join("\n", candidates)
        );

        String aiText = deepSeekAiService.chat(systemPrompt, userPrompt);
        AiConsultResponse parsed = parseResponse(aiText);

        AiConsultation consultation = new AiConsultation();
        consultation.setPatientId(request.getPatientId());
        consultation.setChiefComplaint(request.getContent());
        consultation.setSymptomDetail(request.getContent());
        consultation.setAiSummary(parsed.getSummary());
        consultation.setRiskLevel(normalizeRiskLevel(parsed.getRiskLevel()));
        consultation.setRecommendedDeptId(matchDepartmentId(parsed.getRecommendedDept(), departments));
        consultation.setRecommendedDeptName(matchDepartmentName(consultation.getRecommendedDeptId(), departments));
        consultation.setAiResult(parsed.getAiResult() == null || parsed.getAiResult().isBlank() ? aiText : parsed.getAiResult());
        consultation.setStatus(EnumValues.AI_GENERATED);
        aiConsultationMapper.insert(consultation);
        return consultation;
    }

    @Override public List<AiConsultation> getMyConsultations(Long patientId) { return aiConsultationMapper.findByPatientId(patientId); }

    private AiConsultResponse parseResponse(String aiText) {
        AiConsultResponse response = new AiConsultResponse();
        response.setAiResult(aiText);
        response.setSummary("");
        response.setRiskLevel(EnumValues.RISK_NORMAL);
        response.setRecommendedDept("");
        response.setRecommendedDoctor("");
        response.setNote("");
        try {
            Map<?, ?> map = objectMapper.readValue(aiText, Map.class);
            response.setSummary(stringValue(map.get("summary")));
            response.setRiskLevel(stringValue(map.get("riskLevel")));
            response.setRecommendedDept(stringValue(map.get("recommendedDept")));
            response.setRecommendedDoctor(stringValue(map.get("recommendedDoctor")));
            response.setAiResult(stringValue(map.get("aiResult")));
            response.setNote(stringValue(map.get("note")));
        } catch (Exception ignored) {}
        return response;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String normalizeRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) {
            return EnumValues.RISK_NORMAL;
        }
        String value = riskLevel.trim().toLowerCase();
        if (EnumValues.RISK_URGENT.equals(riskLevel.trim())
                || "urgent".equals(value)
                || "warning".equals(value)
                || "high".equals(value)
                || "critical".equals(value)
                || value.contains("急")
                || value.contains("危")) {
            return EnumValues.RISK_URGENT;
        }
        return EnumValues.RISK_NORMAL;
    }

    private Long matchDepartmentId(String recommendedDept, List<Department> departments) {
        if (recommendedDept == null || recommendedDept.isBlank()) {
            return null;
        }
        String normalized = recommendedDept.trim();
        return departments.stream()
                .filter(d -> normalized.equals(safe(d.getDeptName()).trim()))
                .map(Department::getDeptId)
                .findFirst()
                .orElse(null);
    }

    private String matchDepartmentName(Long recommendedDeptId, List<Department> departments) {
        if (recommendedDeptId == null) {
            return "";
        }
        return departments.stream()
                .filter(d -> recommendedDeptId.equals(d.getDeptId()))
                .map(Department::getDeptName)
                .findFirst()
                .orElse("");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
