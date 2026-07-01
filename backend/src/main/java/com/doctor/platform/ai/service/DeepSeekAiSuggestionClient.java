package com.doctor.platform.ai.service;

import com.doctor.platform.ai.config.AiProperties;
import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.ai.dto.ExamLabReportContext;
import com.doctor.platform.infrastructure.exception.BusinessException;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.doctor.platform.modules.outpatient.entity.MedicalRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekAiSuggestionClient implements AiSuggestionClient {

    private static final String SYSTEM_PROMPT = """
        你是门诊医生工作站的 AI 辅助助手，只能提供临床辅助建议，不能替代医生最终判断。
        请基于输入的患者资料、历史病历、本次问诊和检查检验医嘱生成建议。
        必须只返回一个合法 JSON 对象，不要使用 Markdown，不要输出解释性前后缀。
        JSON 字段固定为：sceneCode, backgroundSummary, diagnosisDraft, planDraft,
        evidence, examSuggestions, possibleDiagnoses, examRecommendations, drugSuggestions, riskFlags, reportSummary。
        evidence、examSuggestions、drugSuggestions、riskFlags 必须是字符串数组。
        possibleDiagnoses 必须是对象数组，元素字段固定为 name, reason。
        examRecommendations 必须是对象数组，元素字段固定为 type, name, reason。
        当 sceneCode=OUTPATIENT_INITIAL_SUGGESTION 时：
        1. diagnosisDraft 只输出最可能的 1-2 条诊断建议。
        2. possibleDiagnoses 最多输出 3 条候选诊断，按可能性从高到低排序。
        3. examRecommendations 只输出检查或检验项目及原因，不输出治疗建议。
        4. planDraft 必须为空字符串。
        5. drugSuggestions 必须为空数组。
        6. evidence 只引用当前病历和当前医生填写的信息，不要输出统计性废话。
        """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiProperties properties;

    public DeepSeekAiSuggestionClient(RestClient.Builder restClientBuilder,
                                      ObjectMapper objectMapper,
                                      AiProperties properties) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public AiDraftBlock generateSuggestion(AiSuggestionRequest request,
                                           Patient patient,
                                           List<MedicalRecord> recentRecords,
                                           List<ExamLabOrder> recentOrders,
                                           List<ExamLabReportContext> recentReports) {
        try {
            JsonNode response = restClient.post()
                .uri(chatCompletionsUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .body(buildRequestBody(request, patient, recentRecords, recentOrders, recentReports))
                .retrieve()
                .body(JsonNode.class);

            String content = extractMessageContent(response);
            AiDraftBlock block = objectMapper.readValue(normalizeJsonContent(content), AiDraftBlock.class);
            return normalizeBlock(block, request.getSceneCode());
        } catch (RestClientException ex) {
            throw new BusinessException(502, "DeepSeek API 调用失败：" + ex.getMessage());
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new BusinessException(502, "DeepSeek API 响应解析失败：" + ex.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(AiSuggestionRequest request,
                                                 Patient patient,
                                                 List<MedicalRecord> recentRecords,
                                                 List<ExamLabOrder> recentOrders,
                                                 List<ExamLabReportContext> recentReports) throws JsonProcessingException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", modelName());
        body.put("messages", List.of(
            Map.of("role", "system", "content", SYSTEM_PROMPT),
            Map.of("role", "user", "content", buildUserPrompt(request, patient, recentRecords, recentOrders, recentReports))
        ));
        body.put("temperature", 0.2);
        body.put("response_format", Map.of("type", "json_object"));
        return body;
    }

    private String buildUserPrompt(AiSuggestionRequest request,
                                   Patient patient,
                                   List<MedicalRecord> recentRecords,
                                   List<ExamLabOrder> recentOrders,
                                   List<ExamLabReportContext> recentReports) throws JsonProcessingException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sceneCode", request.getSceneCode());
        payload.put("currentVisit", requestSnapshot(request));
        payload.put("patient", patientSnapshot(patient));
        payload.put("recentMedicalRecords", recentRecords.stream().map(this::recordSnapshot).toList());
        payload.put("recentExamLabOrders", recentOrders.stream().map(this::orderSnapshot).toList());
        payload.put("recentExamLabReports", recentReports.stream().map(this::reportSnapshot).toList());
        return objectMapper.writeValueAsString(payload);
    }

    private Map<String, Object> requestSnapshot(AiSuggestionRequest request) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("visitId", request.getVisitId());
        snapshot.put("recordId", request.getRecordId());
        snapshot.put("orderId", request.getOrderId());
        snapshot.put("reportId", request.getReportId());
        snapshot.put("chiefComplaint", request.getCurrentChiefComplaint());
        snapshot.put("presentIllness", request.getCurrentPresentIllness());
        snapshot.put("physicalExam", request.getCurrentPhysicalExam());
        snapshot.put("currentDiagnosis", request.getCurrentDiagnosis());
        return snapshot;
    }

    private Map<String, Object> patientSnapshot(Patient patient) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("patientId", patient.getPatientId());
        snapshot.put("patientName", patient.getPatientName());
        snapshot.put("gender", patient.getGender());
        snapshot.put("birthday", patient.getBirthday());
        snapshot.put("allergyHistory", patient.getAllergyHistory());
        snapshot.put("pastHistory", patient.getPastHistory());
        return snapshot;
    }

    private Map<String, Object> recordSnapshot(MedicalRecord record) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("recordId", record.getRecordId());
        snapshot.put("visitId", record.getVisitId());
        snapshot.put("chiefComplaint", record.getChiefComplaint());
        snapshot.put("presentIllness", record.getPresentIllness());
        snapshot.put("physicalExam", record.getPhysicalExam());
        snapshot.put("auxiliaryExam", record.getAuxiliaryExam());
        snapshot.put("diagnosis", record.getDiagnosis());
        snapshot.put("treatmentAdvice", record.getTreatmentAdvice());
        snapshot.put("updatedAt", record.getUpdatedAt());
        return snapshot;
    }

    private Map<String, Object> orderSnapshot(ExamLabOrder order) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderId", order.getOrderId());
        snapshot.put("orderType", order.getOrderType());
        snapshot.put("clinicalDiagnosis", order.getClinicalDiagnosis());
        snapshot.put("purpose", order.getPurpose());
        snapshot.put("status", order.getStatus());
        snapshot.put("completedAt", order.getCompletedAt());
        return snapshot;
    }

    private Map<String, Object> reportSnapshot(ExamLabReportContext report) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("reportId", report.getReportId());
        snapshot.put("orderId", report.getOrderId());
        snapshot.put("orderItemId", report.getOrderItemId());
        snapshot.put("orderNo", report.getOrderNo());
        snapshot.put("orderType", report.getOrderType());
        snapshot.put("itemName", report.getItemName());
        snapshot.put("itemType", report.getItemType());
        snapshot.put("resultSummary", report.getResultSummary());
        snapshot.put("reportNo", report.getReportNo());
        snapshot.put("reportType", report.getReportType());
        snapshot.put("findings", report.getFindings());
        snapshot.put("conclusion", report.getConclusion());
        snapshot.put("doctorReview", report.getDoctorReview());
        snapshot.put("status", report.getStatus());
        snapshot.put("publishedAt", report.getPublishedAt());
        return snapshot;
    }

    private String chatCompletionsUrl() {
        String baseUrl = StringUtils.hasText(properties.getBaseUrl())
            ? properties.getBaseUrl().trim()
            : "https://api.deepseek.com";
        String normalized = baseUrl.replaceAll("/+$", "");
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        return normalized + "/chat/completions";
    }

    private String modelName() {
        return StringUtils.hasText(properties.getModelName())
            ? properties.getModelName().trim()
            : "deepseek-chat";
    }

    private String extractMessageContent(JsonNode response) {
        if (response == null) {
            throw new IllegalArgumentException("响应为空");
        }
        String content = response.path("choices").path(0).path("message").path("content").asText(null);
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("choices[0].message.content 为空");
        }
        return content;
    }

    private String normalizeJsonContent(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            return trimmed.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        return trimmed;
    }

    private AiDraftBlock normalizeBlock(AiDraftBlock block, String sceneCode) {
        if (!StringUtils.hasText(block.getSceneCode())) {
            block.setSceneCode(sceneCode);
        }
        if (block.getEvidence() == null) {
            block.setEvidence(List.of());
        }
        if (block.getExamSuggestions() == null) {
            block.setExamSuggestions(List.of());
        }
        if (block.getPossibleDiagnoses() == null) {
            block.setPossibleDiagnoses(List.of());
        }
        if (block.getExamRecommendations() == null) {
            block.setExamRecommendations(List.of());
        }
        if (block.getDrugSuggestions() == null) {
            block.setDrugSuggestions(List.of());
        }
        if (block.getRiskFlags() == null) {
            block.setRiskFlags(List.of());
        }
        if ("OUTPATIENT_INITIAL_SUGGESTION".equals(sceneCode)) {
            block.setPlanDraft("");
            block.setDrugSuggestions(List.of());
        }
        return block;
    }
}
