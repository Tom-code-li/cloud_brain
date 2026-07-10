package com.hospital.ai.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.ai.common.RoleAiConfig;
import com.hospital.common.core.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekRegistrationAiClient {
    private static final String REGISTRATION_API_KEY_REF = "DEEPSEEK_API_KEY_REGISTRATION";
    private static final String REGISTRATION_MODEL_REF = "DEEPSEEK_MODEL_REGISTRATION";
    private static final String DEFAULT_MODEL = "deepseek-chat";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final boolean enabled;
    private final int timeoutSeconds;
    private final String defaultModelName;

    public DeepSeekRegistrationAiClient(
            ObjectMapper objectMapper,
            @Value("${ai.deepseek.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${ai.deepseek.registration.enabled:true}") boolean enabled,
            @Value("${ai.deepseek.timeout-seconds:20}") int timeoutSeconds,
            @Value("${ai.deepseek.registration.model-name:deepseek-chat}") String defaultModelName
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(3, timeoutSeconds)))
                .build();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.enabled = enabled;
        this.timeoutSeconds = timeoutSeconds;
        this.defaultModelName = defaultModelName == null || defaultModelName.isBlank()
                ? DEFAULT_MODEL
                : defaultModelName.trim();
    }

    public boolean available(RoleAiConfig config) {
        return enabled && !apiKey(config).isBlank();
    }

    public String unavailableReason(RoleAiConfig config) {
        if (!enabled) {
            return "ai.deepseek.registration.enabled=false";
        }
        if (apiKey(config).isBlank()) {
            return "未配置 " + apiKeyRef(config);
        }
        return "";
    }

    public List<String> complete(RoleAiConfig config, String sceneCode, Map<String, Object> contextData) {
        String apiKey = apiKey(config);
        if (apiKey.isBlank()) {
            throw new BusinessException("DeepSeek API Key 未配置，请检查 " + apiKeyRef(config));
        }
        String modelName = modelName(config);
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt(sceneCode)),
                            Map.of("role", "user", "content", userPrompt(sceneCode, contextData))
                    ),
                    "temperature", 0.2,
                    "max_tokens", 1200,
                    "stream", false
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(Math.max(3, timeoutSeconds)))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("DeepSeek 调用失败，HTTP " + response.statusCode() + "：" + response.body());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content.isBlank()) {
                throw new BusinessException("DeepSeek 返回内容为空");
            }
            String cleanedContent = cleanContent(content);
            return List.of(
                    "AI 辅助内容仅供挂号医生参考，最终结论必须由医生确认。\n\n",
                    "当前 AI 模式：外部 DeepSeek / " + modelName + " / " + apiKeyRef(config) + "。\n\n",
                    cleanedContent + "\n\n"
            );
        } catch (IOException ex) {
            throw new BusinessException("DeepSeek 响应解析失败：" + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("DeepSeek 调用被中断");
        }
    }

    private String systemPrompt(String sceneCode) {
        return """
                你是东软云医院 HIS 挂号医生端的 AI 分诊助手。
                你只能做分诊建议、推荐科室、推荐医生，不能直接挂号、收费、退费、叫号或改变业务状态。
                必须优先依据患者主诉、性别、过敏史、既往史、可选科室、医生专长和排班余号判断，不能只复述页面已选项。
                推荐科室必须只能从上下文 departments 列表中选择，不允许推荐 departments 列表外的科室名称。
                如果主诉明显需要某专科但 departments 列表中没有对应科室，一律推荐全科门诊；若没有全科门诊，则推荐 departments 中第一个 OUTPATIENT 科室。
                对骨折、外伤、扭伤、关节疼痛等骨外科方向主诉，如果上下文 departments 没有骨科、外科或急诊，只能推荐全科门诊，不能推荐呼吸内科，也不能建议“调整到骨科”作为当前挂号结论。
                如果主诉与当前已选科室、医生或排班不匹配，要明确提醒挂号医生先调整科室、医生或排班。
                发现胸痛、呼吸困难、意识异常、持续呕吐、明显脱水、高热不退、咯血、抽搐等风险信号时，要提示人工优先复核或考虑急诊。
                用中文纯文本输出，不要输出 JSON，不要输出 Markdown 表格。
                不要使用 Markdown 粗体、标题、代码块或 emoji；编号可以使用“1. 2. 3. 4.”。
                输出结构：
                1. 分诊优先级
                2. 推荐科室和理由
                3. 推荐医生或排班建议
                4. 风险提醒和需要医生确认的事项
                """.strip() + "\n当前场景：" + sceneCode;
    }

    private String userPrompt(String sceneCode, Map<String, Object> contextData) throws IOException {
        return "请根据以下挂号端上下文生成 " + sceneCode + " 建议。上下文中 query 字段如果是 JSON 字符串，请先解析后再判断：\n"
                + objectMapper.writeValueAsString(contextData == null ? Map.of() : contextData);
    }

    private String cleanContent(String content) {
        return content.strip()
                .replace("**", "")
                .replace("__", "")
                .replace("###", "")
                .replace("##", "")
                .replace("⚠️", "")
                .replace("⚠", "")
                .replaceAll("(?m)^\\s*[-*]\\s+", "");
    }

    private String apiKey(RoleAiConfig config) {
        String registrationKey = setting(REGISTRATION_API_KEY_REF);
        if (!registrationKey.isBlank()) {
            return registrationKey;
        }

        String keyRef = config == null ? "" : config.apiKeyRef();
        if (keyRef != null && !keyRef.isBlank() && !REGISTRATION_API_KEY_REF.equals(keyRef)) {
            return setting(keyRef);
        }
        return "";
    }

    private String modelName(RoleAiConfig config) {
        String registrationModel = setting(REGISTRATION_MODEL_REF);
        if (!registrationModel.isBlank()) {
            return registrationModel;
        }
        if (config != null
                && "deepseek".equalsIgnoreCase(config.provider())
                && config.modelName() != null
                && !config.modelName().isBlank()) {
            return config.modelName().trim();
        }
        return defaultModelName;
    }

    private String apiKeyRef(RoleAiConfig config) {
        if (!setting(REGISTRATION_API_KEY_REF).isBlank()) {
            return REGISTRATION_API_KEY_REF;
        }
        String keyRef = config == null ? "" : config.apiKeyRef();
        return keyRef == null || keyRef.isBlank() ? REGISTRATION_API_KEY_REF : keyRef;
    }

    private String setting(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return System.getProperty(key, "").trim();
    }

    private String trimTrailingSlash(String value) {
        String text = value == null || value.isBlank() ? "https://api.deepseek.com" : value.trim();
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
