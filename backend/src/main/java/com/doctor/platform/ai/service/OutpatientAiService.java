package com.doctor.platform.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.ai.config.AiProperties;
import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.AiSceneCode;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.ai.dto.ExamRecommendationItem;
import com.doctor.platform.ai.dto.ExamLabReportContext;
import com.doctor.platform.ai.dto.PossibleDiagnosisItem;
import com.doctor.platform.ai.entity.AiCallLog;
import com.doctor.platform.ai.mapper.AiCallLogMapper;
import com.doctor.platform.infrastructure.exception.BusinessException;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.examlab.entity.ExamLabOrderItem;
import com.doctor.platform.examlab.entity.ExamLabReport;
import com.doctor.platform.examlab.mapper.ExamLabOrderItemMapper;
import com.doctor.platform.examlab.mapper.ExamLabOrderMapper;
import com.doctor.platform.examlab.mapper.ExamLabReportMapper;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.doctor.platform.modules.outpatient.mapper.PatientMapper;
import com.doctor.platform.modules.outpatient.entity.MedicalRecord;
import com.doctor.platform.modules.outpatient.mapper.MedicalRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OutpatientAiService {

    private static final String OUTPATIENT_AI_ROLE_CODE = "OUTPATIENT_AI";
    private static final String AI_CALL_STATUS_SUCCESS = "成功";

    private final PatientMapper patientMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final ExamLabOrderMapper examLabOrderMapper;
    private final ExamLabOrderItemMapper examLabOrderItemMapper;
    private final ExamLabReportMapper examLabReportMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final AiProperties aiProperties;
    private final AiSuggestionClient aiSuggestionClient;

    public OutpatientAiService(PatientMapper patientMapper,
                               MedicalRecordMapper medicalRecordMapper,
                               ExamLabOrderMapper examLabOrderMapper,
                               ExamLabOrderItemMapper examLabOrderItemMapper,
                               ExamLabReportMapper examLabReportMapper,
                               AiCallLogMapper aiCallLogMapper,
                               AiProperties aiProperties,
                               AiSuggestionClient aiSuggestionClient) {
        this.patientMapper = patientMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.examLabOrderMapper = examLabOrderMapper;
        this.examLabOrderItemMapper = examLabOrderItemMapper;
        this.examLabReportMapper = examLabReportMapper;
        this.aiCallLogMapper = aiCallLogMapper;
        this.aiProperties = aiProperties;
        this.aiSuggestionClient = aiSuggestionClient;
    }

    public AiDraftBlock generateSuggestion(AiSuggestionRequest request) {
        Patient patient = patientMapper.selectById(request.getPatientId());
        if (patient == null) {
            throw new BusinessException(404, "患者不存在");
        }
        List<MedicalRecord> recentRecords = medicalRecordMapper.selectList(
            new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPatientId, request.getPatientId())
                .orderByDesc(MedicalRecord::getUpdatedAt)
                .last("limit 5")
        );
        List<ExamLabOrder> recentOrders = examLabOrderMapper.selectList(
            new LambdaQueryWrapper<ExamLabOrder>()
                .eq(ExamLabOrder::getPatientId, request.getPatientId())
                .orderByDesc(ExamLabOrder::getUpdatedAt)
                .last("limit 5")
        );
        List<ExamLabReportContext> recentReports = loadReportContexts(request);
        if (AiSceneCode.OUTPATIENT_POST_REPORT_SUGGESTION.equals(request.getSceneCode()) && recentReports.isEmpty()) {
            throw new BusinessException(404, "暂无已发布的检查/检验报告，不能生成基于真实结果的 AI 分析");
        }

        boolean useDeepSeek = shouldUseDeepSeek();
        AiDraftBlock block = useDeepSeek
            ? aiSuggestionClient.generateSuggestion(request, patient, recentRecords, recentOrders, recentReports)
            : buildSimulatedSuggestion(request, patient, recentRecords, recentOrders, recentReports);

        saveAiLog(request, block, useDeepSeek);
        return block;
    }

    private AiDraftBlock buildSimulatedSuggestion(AiSuggestionRequest request,
                                                  Patient patient,
                                                  List<MedicalRecord> recentRecords,
                                                  List<ExamLabOrder> recentOrders,
                                                  List<ExamLabReportContext> recentReports) {
        return switch (request.getSceneCode()) {
            case AiSceneCode.OUTPATIENT_BACKGROUND_SUMMARY -> buildBackgroundSummary(patient, recentRecords, recentOrders);
            case AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION -> buildInitialSuggestion(patient, recentRecords, request);
            case AiSceneCode.OUTPATIENT_POST_REPORT_SUGGESTION -> buildPostReportSuggestion(patient, recentRecords, recentOrders, recentReports);
            default -> AiDraftBlock.builder()
                .sceneCode(request.getSceneCode())
                .backgroundSummary("暂未识别的 AI 场景")
                .evidence(List.of())
                .examSuggestions(List.of())
                .drugSuggestions(List.of())
                .riskFlags(List.of())
                .build();
        };
    }

    private AiDraftBlock buildBackgroundSummary(Patient patient,
                                                List<MedicalRecord> recentRecords,
                                                List<ExamLabOrder> recentOrders) {
        String latestDiagnosis = recentRecords.stream()
            .map(MedicalRecord::getDiagnosis)
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse("暂无既往明确诊断");
        String latestOrder = recentOrders.stream()
            .map(ExamLabOrder::getOrderType)
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse("暂无近期检查检验单");

        List<String> risks = new ArrayList<>();
        if (StringUtils.hasText(patient.getAllergyHistory())) {
            risks.add("存在过敏史，需复核用药禁忌");
        }
        if (StringUtils.hasText(patient.getPastHistory())) {
            risks.add("既往史需纳入诊疗决策");
        }

        return AiDraftBlock.builder()
            .sceneCode(AiSceneCode.OUTPATIENT_BACKGROUND_SUMMARY)
            .backgroundSummary("近 3-5 次病历摘要已整理：最近诊断为“" + latestDiagnosis + "”，近期医嘱类型为“" + latestOrder + "”。")
            .evidence(List.of("过敏史：" + nullSafe(patient.getAllergyHistory()), "既往史：" + nullSafe(patient.getPastHistory())))
            .riskFlags(risks)
            .examSuggestions(List.of())
            .possibleDiagnoses(List.of())
            .examRecommendations(List.of())
            .drugSuggestions(List.of())
            .build();
    }

    private AiDraftBlock buildInitialSuggestion(Patient patient,
                                                List<MedicalRecord> recentRecords,
                                                AiSuggestionRequest request) {
        String diagnosisDraft = "急性上呼吸道感染";
        if (StringUtils.hasText(request.getCurrentDiagnosis())) {
            diagnosisDraft = request.getCurrentDiagnosis();
        }

        return AiDraftBlock.builder()
            .sceneCode(AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION)
            .backgroundSummary("已结合既往病历、过敏史与本次问诊内容生成初诊草稿。")
            .diagnosisDraft(diagnosisDraft)
            .planDraft("")
            .evidence(List.of(
                "主诉：" + nullSafe(request.getCurrentChiefComplaint()),
                "现病史：" + nullSafe(request.getCurrentPresentIllness()),
                "既往史：" + nullSafe(patient.getPastHistory()),
                "体格检查：" + nullSafe(request.getCurrentPhysicalExam()),
                "过敏史：" + nullSafe(patient.getAllergyHistory())
            ))
            .possibleDiagnoses(List.of(
                PossibleDiagnosisItem.builder()
                    .name(diagnosisDraft)
                    .reason("主诉与现病史提示急性起病的上呼吸道感染表现。")
                    .build(),
                PossibleDiagnosisItem.builder()
                    .name("早期社区获得性肺炎")
                    .reason("若后续出现持续高热、呼吸困难或影像学新发异常，需要进一步排查。")
                    .build()
            ))
            .examSuggestions(List.of("血常规", "C反应蛋白", "胸部正位片"))
            .examRecommendations(List.of(
                ExamRecommendationItem.builder()
                    .type("检验")
                    .name("血常规")
                    .reason("辅助判断感染程度和白细胞变化。")
                    .build(),
                ExamRecommendationItem.builder()
                    .type("检验")
                    .name("C反应蛋白")
                    .reason("辅助评估炎症活动度和感染倾向。")
                    .build(),
                ExamRecommendationItem.builder()
                    .type("检查")
                    .name("胸部正位片")
                    .reason("用于排查肺部影像学异常，辅助鉴别肺炎。")
                    .build()
            ))
            .drugSuggestions(List.of())
            .riskFlags(buildRiskFlags(patient, recentRecords))
            .build();
    }

    private AiDraftBlock buildPostReportSuggestion(Patient patient,
                                                   List<MedicalRecord> recentRecords,
                                                   List<ExamLabOrder> recentOrders,
                                                   List<ExamLabReportContext> recentReports) {
        String reportSummary = buildReportSummary(recentReports);

        return AiDraftBlock.builder()
            .sceneCode(AiSceneCode.OUTPATIENT_POST_REPORT_SUGGESTION)
            .reportSummary(reportSummary)
            .diagnosisDraft(buildReportDiagnosisDraft(recentReports))
            .planDraft("建议复核感染指标变化，必要时调整抗感染方案。")
            .evidence(buildReportEvidence(patient, recentRecords, recentOrders, recentReports))
            .examSuggestions(List.of("复查血常规", "必要时复查胸部CT"))
            .possibleDiagnoses(List.of())
            .examRecommendations(List.of())
            .drugSuggestions(List.of("根据药敏或临床反应调整抗生素"))
            .riskFlags(buildRiskFlags(patient, recentRecords))
            .build();
    }

    private List<ExamLabReportContext> loadReportContexts(AiSuggestionRequest request) {
        LambdaQueryWrapper<ExamLabReport> query = new LambdaQueryWrapper<ExamLabReport>()
            .eq(ExamLabReport::getPatientId, request.getPatientId())
            .orderByDesc(ExamLabReport::getPublishedAt)
            .orderByDesc(ExamLabReport::getCreatedAt)
            .last("limit 10");

        if (request.getReportId() != null) {
            query.eq(ExamLabReport::getReportId, request.getReportId());
        }
        if (request.getOrderId() != null) {
            query.eq(ExamLabReport::getOrderId, request.getOrderId());
        }

        List<ExamLabReport> reports = examLabReportMapper.selectList(query);
        if (request.getVisitId() != null) {
            reports = reports.stream()
                .filter(report -> {
                    ExamLabOrder order = examLabOrderMapper.selectById(report.getOrderId());
                    return order != null && request.getVisitId().equals(order.getVisitId());
                })
                .toList();
        }

        return reports.stream().map(this::toReportContext).toList();
    }

    private ExamLabReportContext toReportContext(ExamLabReport report) {
        ExamLabOrder order = report.getOrderId() == null ? null : examLabOrderMapper.selectById(report.getOrderId());
        ExamLabOrderItem item = report.getOrderItemId() == null ? null : examLabOrderItemMapper.selectById(report.getOrderItemId());

        return ExamLabReportContext.builder()
            .reportId(report.getReportId())
            .orderId(report.getOrderId())
            .orderItemId(report.getOrderItemId())
            .orderNo(order == null ? null : order.getOrderNo())
            .orderType(order == null ? null : order.getOrderType())
            .itemName(item == null ? null : item.getItemName())
            .itemType(item == null ? null : item.getItemType())
            .resultSummary(item == null ? null : item.getResultSummary())
            .reportNo(report.getReportNo())
            .reportType(report.getReportType())
            .findings(report.getFindings())
            .conclusion(report.getConclusion())
            .aiDraft(report.getAiDraft())
            .doctorReview(report.getDoctorReview())
            .status(report.getStatus())
            .publishedAt(report.getPublishedAt())
            .build();
    }

    private String buildReportSummary(List<ExamLabReportContext> recentReports) {
        return recentReports.stream()
            .map(report -> String.join("；",
                "项目：" + nullSafe(report.getItemName()),
                "所见：" + nullSafe(report.getFindings()),
                "结论：" + nullSafe(report.getConclusion())
            ))
            .reduce((left, right) -> left + "\n" + right)
            .orElse("暂无检查/检验报告结果");
    }

    private String buildReportDiagnosisDraft(List<ExamLabReportContext> recentReports) {
        return recentReports.stream()
            .map(ExamLabReportContext::getConclusion)
            .filter(StringUtils::hasText)
            .findFirst()
            .orElse("请结合真实检查/检验报告完善正式诊断。");
    }

    private List<String> buildReportEvidence(Patient patient,
                                             List<MedicalRecord> recentRecords,
                                             List<ExamLabOrder> recentOrders,
                                             List<ExamLabReportContext> recentReports) {
        List<String> evidence = new ArrayList<>();
        evidence.add("近期医嘱数量：" + recentOrders.size());
        evidence.add("既往病历数量：" + recentRecords.size());
        evidence.add("过敏史：" + nullSafe(patient.getAllergyHistory()));
        recentReports.forEach(report -> evidence.add(String.join("；",
            "报告号：" + nullSafe(report.getReportNo()),
            "项目：" + nullSafe(report.getItemName()),
            "结果摘要：" + nullSafe(report.getResultSummary()),
            "所见：" + nullSafe(report.getFindings()),
            "结论：" + nullSafe(report.getConclusion())
        )));
        return evidence;
    }

    private List<String> buildRiskFlags(Patient patient, List<MedicalRecord> recentRecords) {
        List<String> flags = new ArrayList<>();
        if (StringUtils.hasText(patient.getAllergyHistory())) {
            flags.add("存在药物/食物过敏史");
        }
        if (!recentRecords.isEmpty()) {
            flags.add("存在既往就诊记录，建议结合最近 3-5 次病历综合判断");
        }
        return flags;
    }

    private boolean shouldUseDeepSeek() {
        if (!"deepseek".equalsIgnoreCase(nullSafe(aiProperties.getProvider()))) {
            return false;
        }
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            throw new BusinessException(500, "DeepSeek 已配置为 AI_PROVIDER，但 AI_ENABLED 未开启");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey()) || "replace-me".equals(aiProperties.getApiKey())) {
            throw new BusinessException(500, "DeepSeek API Key 未配置，请设置 AI_API_KEY 或 launch.json env.AI_API_KEY");
        }
        return true;
    }

    private void saveAiLog(AiSuggestionRequest request, AiDraftBlock block, boolean useDeepSeek) {
        AiCallLog log = new AiCallLog();
        log.setPatientId(request.getPatientId());
        log.setRoleCode(OUTPATIENT_AI_ROLE_CODE);
        log.setSceneCode(request.getSceneCode());
        log.setBusinessType("OUTPATIENT_AI_ASSISTANT");
        log.setBusinessId(request.getVisitId());
        log.setPrompt("currentChiefComplaint=" + nullSafe(request.getCurrentChiefComplaint()));
        log.setResponse(block.toString());
        log.setModelName(resolveModelName(useDeepSeek));
        log.setApiKeyRef(useDeepSeek ? "env:AI_API_KEY" : "local-simulated");
        log.setStatus(AI_CALL_STATUS_SUCCESS);
        log.setChangedBusinessStatus(0);
        log.setStartedAt(LocalDateTime.now());
        log.setCompletedAt(LocalDateTime.now());
        aiCallLogMapper.insert(log);
    }

    private String nullSafe(String value) {
        return StringUtils.hasText(value) ? value : "无";
    }

    private String resolveModelName(boolean useDeepSeek) {
        if (useDeepSeek && StringUtils.hasText(aiProperties.getModelName())) {
            return aiProperties.getModelName();
        }
        return "simulated-outpatient-ai";
    }
}
