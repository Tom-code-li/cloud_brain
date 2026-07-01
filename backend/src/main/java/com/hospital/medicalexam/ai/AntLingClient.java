package com.hospital.medicalexam.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "ant-ling")
public class AntLingClient implements AiDraftService {
    private final AntLingProperties properties;
    private final AiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public AntLingClient(AntLingProperties properties, AiPromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generate(AiDraftRequest request) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return "";
        }
        try {
            String prompt = promptBuilder.build(request);
            String rawJson = callModel(prompt);
            if (rawJson == null || rawJson.isBlank()) {
                return "";
            }
            AiDraftResult result = objectMapper.readValue(rawJson, AiDraftResult.class);
            return format(result);
        } catch (Exception ignored) {
            return "";
        }
    }

    private String callModel(String prompt) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .baseUrl(normalizeBaseUrl(properties.baseUrl()))
                .timeout(Duration.ofSeconds(timeoutSeconds()))
                .maxRetries(1)
                .build();
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(properties.model())
                    .addUserMessage(prompt)
                    .temperature(0.2)
                    .build();
            ChatCompletion completion = client.chat().completions().create(params);
            return completion.choices().stream()
                    .findFirst()
                    .flatMap(choice -> choice.message().content())
                    .orElse("");
        } finally {
            client.close();
        }
    }

    private int timeoutSeconds() {
        if (properties.timeoutSeconds() == null || properties.timeoutSeconds() <= 0) {
            return 30;
        }
        return properties.timeoutSeconds();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.ant-ling.com/v1/";
        }
        return baseUrl.trim();
    }

    private String format(AiDraftResult result) {
        if (result == null || isBlank(result.reportDraft())) {
            return "";
        }
        String abnormal = result.abnormalSummary() == null ? "" : String.join("；", result.abnormalSummary());
        String suggestions = result.reviewSuggestions() == null ? "" : String.join("；", result.reviewSuggestions());
        return """
                AI辅助草稿：
                %s

                可能方向：
                %s

                风险级别：
                %s

                异常摘要：
                %s

                复核建议：
                %s

                提示：AI内容仅供辅助参考，请医生确认后发布。
                """.formatted(
                value(result.reportDraft()),
                value(result.possibleDirection()),
                value(result.riskLevel()),
                value(abnormal),
                value(suggestions)
        ).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String value(String value) {
        return isBlank(value) ? "未提供" : value.trim();
    }
}
