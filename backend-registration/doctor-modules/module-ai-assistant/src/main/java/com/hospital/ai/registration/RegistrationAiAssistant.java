package com.hospital.ai.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.ai.common.AbstractRoleAiAssistant;
import com.hospital.ai.common.RoleAiConfig;
import com.hospital.ai.common.RoleAiConfigRegistry;
import com.hospital.ai.common.RoleAiRequest;
import com.hospital.common.core.BusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RegistrationAiAssistant extends AbstractRoleAiAssistant {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final RoleAiConfigRegistry configRegistry;
    private final DeepSeekRegistrationAiClient deepSeekClient;

    public RegistrationAiAssistant(RoleAiConfigRegistry configRegistry) {
        this(configRegistry, (DeepSeekRegistrationAiClient) null);
    }

    @Autowired
    public RegistrationAiAssistant(
            RoleAiConfigRegistry configRegistry,
            ObjectProvider<DeepSeekRegistrationAiClient> deepSeekClientProvider
    ) {
        this(configRegistry, deepSeekClientProvider.getIfAvailable());
    }

    private RegistrationAiAssistant(
            RoleAiConfigRegistry configRegistry,
            DeepSeekRegistrationAiClient deepSeekClient
    ) {
        super(configRegistry, "REGISTRATION", List.of(
                "TRIAGE",
                "DEPARTMENT_RECOMMEND",
                "DOCTOR_RECOMMEND"
        ));
        this.configRegistry = configRegistry;
        this.deepSeekClient = deepSeekClient;
    }

    @Override
    public List<String> assist(RoleAiRequest request) {
        RoleAiConfig config = configRegistry.roleConfig(roleCode());
        String sceneCode = normalizeScene(request.sceneCode());
        Map<String, Object> context = normalizeContext(request.contextData());
        if (deepSeekClient != null && deepSeekClient.available(config)) {
            try {
                return deepSeekClient.complete(config, sceneCode, context);
            } catch (BusinessException ex) {
                return List.of(
                        SAFETY_PREFIX + "\n\n",
                        "当前 AI 模式：本地规则兜底（外部 DeepSeek 调用失败：" + concise(ex.getMessage()) + "）。\n\n",
                        buildReadableFallback(sceneCode, context)
                );
            }
        }
        String reason = deepSeekClient == null ? "DeepSeek 客户端未加载" : deepSeekClient.unavailableReason(config);
        return List.of(
                SAFETY_PREFIX + "\n\n",
                "当前 AI 模式：本地规则兜底（" + reason + "，未调用外部大模型）。\n\n",
                buildReadableFallback(sceneCode, context)
        );
    }

    private String normalizeScene(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    @Override
    protected String buildScenePrompt(String sceneCode, String context) {
        return switch (sceneCode) {
            case "TRIAGE" -> "挂号医生分诊建议：请结合患者主诉、可预约科室和排班信息，人工确认分诊结果。";
            case "DEPARTMENT_RECOMMEND" -> "挂号医生推荐科室：请根据症状描述推荐合适科室，并由挂号医生确认。";
            case "DOCTOR_RECOMMEND" -> "挂号医生推荐医生：请结合科室、医生排班和医生专长生成推荐医生建议。";
            default -> "挂号医生通用 AI 辅助：仅提供分诊、科室和医生推荐参考。";
        };
    }

    private String buildReadableFallback(String sceneCode, Map<String, Object> context) {
        ContextSummary summary = summarize(context);
        return switch (sceneCode) {
            case "DEPARTMENT_RECOMMEND" -> departmentRecommendation(summary);
            case "DOCTOR_RECOMMEND" -> doctorRecommendation(summary);
            default -> triageRecommendation(summary);
        };
    }

    private String triageRecommendation(ContextSummary summary) {
        return String.join("\n",
                "挂号医生分诊建议：",
                "1. 分诊级别：" + triageLevel(summary.chiefComplaint()) + "。主诉为“" + summary.chiefComplaint() + "”，" + triageAction(summary) + "。",
                "2. 推荐科室：" + summary.deptName() + "。理由：" + summary.deptReason() + "。",
                "3. 推荐医生/排班：" + summary.doctorLabel() + "；可预约：" + summary.scheduleLabel() + "。",
                "4. 操作提醒：若页面当前已选科室、医生或排班与上述建议不一致，请先调整后再提交挂号。",
                "5. 注意事项：" + safetyReminder(summary)
        ) + "\n\n";
    }

    private String departmentRecommendation(ContextSummary summary) {
        return String.join("\n",
                "科室推荐：",
                "1. 首选科室：" + summary.deptName() + "。",
                "2. 推荐理由：" + summary.deptReason() + "。患者主诉为“" + summary.chiefComplaint() + "”。",
                "3. 挂号提醒：若当前已选科室不是首选科室，请先调整科室并重新选择医生、排班；若现场补充出现高热不退、胸痛、呼吸困难、明显脱水等情况，请人工优先复核。"
        ) + "\n\n";
    }

    private String doctorRecommendation(ContextSummary summary) {
        return String.join("\n",
                "医生推荐：",
                "1. 推荐医生：" + summary.doctorLabel() + "。",
                "2. 推荐依据：" + summary.doctorReason() + "。",
                "3. 可预约排班：" + summary.scheduleLabel() + "。",
                "4. 挂号提醒：" + allergyReminder(summary)
        ) + "\n\n";
    }

    private ContextSummary summarize(Map<String, Object> context) {
        List<?> departments = listValue(context, "departments");
        String chiefComplaint = value(context, "chiefComplaint", "未填写主诉");
        DepartmentChoice departmentChoice = chooseDepartment(departments, chiefComplaint);
        Map<String, Object> doctor = firstMap(listValue(context, "doctors"));
        Map<String, Object> schedule = firstMap(listValue(context, "schedules"));

        return new ContextSummary(
                value(context, "patientName", "当前患者"),
                value(context, "gender", "未填写"),
                chiefComplaint,
                value(context, "allergyHistory", "无特殊过敏史"),
                value(context, "pastHistory", "无特殊既往史"),
                value(departmentChoice.department(), "deptName", "全科门诊"),
                departmentChoice.reason(),
                value(doctor, "doctorName", "当前可预约医生"),
                value(doctor, "title", "医生"),
                value(doctor, "specialty", "常见病初诊"),
                value(schedule, "workDate", "待选择日期"),
                value(schedule, "timePeriod", "待选择时段"),
                value(schedule, "remainQuota", "待确认")
        );
    }

    private DepartmentChoice chooseDepartment(List<?> departments, String chiefComplaint) {
        if (containsAny(chiefComplaint, "儿童", "小儿", "婴儿", "幼儿", "宝宝")) {
            return departmentChoice(departments, "主诉涉及儿童患者，优先由儿科完成初诊分流", "儿科", "儿童");
        }
        if (containsAny(chiefComplaint, "呕吐", "腹泻", "腹痛", "恶心", "脱水", "反酸", "胃痛", "便血", "黑便")) {
            return departmentChoice(departments, "主诉偏消化道症状，且出现脱水描述时需先人工评估生命体征和脱水程度", "消化", "胃肠", "全科");
        }
        if (containsAny(chiefComplaint, "咳嗽", "咽痛", "发热", "气促", "喘", "鼻塞", "流涕", "咳痰")) {
            return departmentChoice(departments, "主诉偏呼吸道或感染症状，适合呼吸相关门诊或全科初诊", "呼吸", "全科");
        }
        if (containsAny(chiefComplaint, "胸痛", "心悸", "胸闷", "血压", "头晕", "晕厥")) {
            return departmentChoice(departments, "主诉含心血管或循环风险信号，应先排查急危重风险", "心血管", "心内", "急诊", "全科");
        }
        if (containsAny(chiefComplaint, "皮疹", "瘙痒", "脱屑", "红斑", "荨麻疹", "湿疹")) {
            return departmentChoice(departments, "主诉偏皮肤黏膜表现，适合皮肤科初诊", "皮肤", "全科");
        }
        if (containsAny(chiefComplaint, "外伤", "骨折", "扭伤", "关节", "腰痛", "腿痛", "肩痛")) {
            return departmentChoice(departments, "主诉偏骨关节或外伤问题，适合骨科或外科方向评估", "骨科", "外科", "全科");
        }
        if (containsAny(chiefComplaint, "尿频", "尿急", "尿痛", "血尿", "腰酸")) {
            return departmentChoice(departments, "主诉偏泌尿系统症状，适合泌尿或肾内方向评估", "泌尿", "肾内", "全科");
        }
        if (containsAny(chiefComplaint, "月经", "阴道", "孕", "妊娠", "腹坠", "白带")) {
            return departmentChoice(departments, "主诉偏妇产科问题，适合妇科方向初诊", "妇科", "产科", "全科");
        }
        if (containsAny(chiefComplaint, "眼痛", "视力", "流泪", "眼红")) {
            return departmentChoice(departments, "主诉偏眼部症状，适合眼科初诊", "眼科", "全科");
        }
        if (containsAny(chiefComplaint, "耳痛", "耳鸣", "鼻出血", "鼻炎", "喉咙", "吞咽")) {
            return departmentChoice(departments, "主诉偏耳鼻咽喉症状，适合耳鼻喉科初诊", "耳鼻喉", "耳鼻咽喉", "全科");
        }
        return departmentChoice(departments, "当前主诉未出现明确专科关键词，可由全科或普通门诊先行初诊分流", "全科", "普通");
    }

    private DepartmentChoice departmentChoice(List<?> departments, String reason, String... keywords) {
        Map<String, Object> department = findDepartmentByKeywords(departments, keywords);
        if (department.isEmpty()) {
            department = generalOutpatientDepartment(departments);
        }
        if (department.isEmpty()) {
            department = findByValue(departments, "deptType", "OUTPATIENT");
        }
        if (department.isEmpty()) {
            department = firstMap(departments);
        }
        return new DepartmentChoice(department, reason);
    }

    private Map<String, Object> generalOutpatientDepartment(List<?> departments) {
        Map<String, Object> department = findDepartmentByKeywords(departments, "全科", "综合", "普通");
        if (!department.isEmpty()) {
            return department;
        }
        return findByValue(departments, "deptType", "OUTPATIENT");
    }

    private Map<String, Object> findDepartmentByKeywords(List<?> values, String... keywords) {
        for (Object item : values) {
            if (item instanceof Map<?, ?> map) {
                String text = value(map, "deptName", "") + " "
                        + value(map, "deptCode", "") + " "
                        + value(map, "description", "");
                if (containsAny(text, keywords)) {
                    return castMap(map);
                }
            }
        }
        return Map.of();
    }

    private Map<String, Object> normalizeContext(Map<String, Object> contextData) {
        Map<String, Object> source = contextData == null ? Map.of() : contextData;
        Object query = source.get("query");
        if (query instanceof String queryText) {
            String trimmed = queryText.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                try {
                    return OBJECT_MAPPER.readValue(trimmed, MAP_TYPE);
                } catch (JsonProcessingException ignored) {
                    return Map.of("chiefComplaint", trimmed);
                }
            }
            if (!trimmed.isBlank()) {
                return Map.of("chiefComplaint", trimmed);
            }
        }
        return source;
    }

    private List<?> listValue(Map<String, Object> context, String key) {
        Object value = context.get(key);
        return value instanceof List<?> list ? list : List.of();
    }

    private Map<String, Object> findByValue(List<?> values, String key, String expectedValue) {
        for (Object item : values) {
            if (item instanceof Map<?, ?> map && expectedValue.equals(value(map, key, ""))) {
                return castMap(map);
            }
        }
        return Map.of();
    }

    private Map<String, Object> firstMap(List<?> values) {
        for (Object item : values) {
            if (item instanceof Map<?, ?> map) {
                return castMap(map);
            }
        }
        return Map.of();
    }

    private Map<String, Object> castMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(map.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue,
                        (left, right) -> right
                )));
    }

    private String value(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        String text = value == null ? "" : String.valueOf(value).trim();
        return text.isBlank() ? fallback : text;
    }

    private String triageLevel(String chiefComplaint) {
        if (containsAny(chiefComplaint, "呼吸困难", "胸痛", "意识", "咯血", "剧烈", "高热", "脱水", "持续呕吐", "不能进食", "尿少", "便血", "黑便")) {
            return "建议优先人工复核";
        }
        return "普通门诊";
    }

    private String triageAction(ContextSummary summary) {
        if ("建议优先人工复核".equals(triageLevel(summary.chiefComplaint()))) {
            return "建议挂号前先由人工复核生命体征、脱水程度或急危重风险，再决定普通门诊或急诊通道";
        }
        return "可按普通门诊流程处理，并结合现场补充信息确认科室";
    }

    private String safetyReminder(ContextSummary summary) {
        String allergy = allergyReminder(summary);
        if (containsAny(summary.chiefComplaint(), "脱水", "持续呕吐", "不能进食", "尿少")) {
            return allergy + "；主诉含脱水或持续呕吐风险信号，请询问饮水、尿量、精神状态，必要时优先复核是否转急诊处理。";
        }
        if (containsAny(summary.chiefComplaint(), "呼吸困难", "胸痛", "咯血", "高热")) {
            return allergy + "；主诉含风险信号，请在挂号前人工复核是否需要急诊或优先处理。";
        }
        return allergy + "；如现场补充出现高热不退、胸痛、呼吸困难等情况，请及时升级分诊。";
    }

    private String allergyReminder(ContextSummary summary) {
        return "过敏史：" + summary.allergyHistory() + "，既往史：" + summary.pastHistory();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String concise(String message) {
        String text = message == null ? "未知错误" : message.replaceAll("\\s+", " ").trim();
        if (text.length() > 180) {
            return text.substring(0, 180) + "...";
        }
        return text;
    }

    private record DepartmentChoice(
            Map<String, Object> department,
            String reason
    ) {
    }

    private record ContextSummary(
            String patientName,
            String gender,
            String chiefComplaint,
            String allergyHistory,
            String pastHistory,
            String deptName,
            String deptReason,
            String doctorName,
            String doctorTitle,
            String doctorSpecialty,
            String workDate,
            String timePeriod,
            String remainQuota
    ) {
        private String doctorLabel() {
            return doctorName + "（" + doctorTitle + "，擅长：" + doctorSpecialty + "）";
        }

        private String doctorReason() {
            return "医生专长为“" + doctorSpecialty + "”，与当前主诉匹配";
        }

        private String scheduleLabel() {
            return workDate + " " + timePeriod + "，余号 " + remainQuota;
        }
    }
}
