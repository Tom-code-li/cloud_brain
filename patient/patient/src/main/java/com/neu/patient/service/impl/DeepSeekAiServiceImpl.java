package com.neu.patient.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.patient.config.DeepSeekProperties;
import com.neu.patient.service.DeepSeekAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class DeepSeekAiServiceImpl implements DeepSeekAiService {
    @Autowired private WebClient deepSeekWebClient;
    @Autowired private DeepSeekProperties properties;
    @Autowired private ObjectMapper objectMapper;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank() || "CHANGE_ME".equals(apiKey)) {
            throw new RuntimeException("DeepSeek API 密钥未配置，请在 application.properties 中设置 deepseek.api-key");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "stream", false
            );

            String response = deepSeekWebClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                throw new RuntimeException("DeepSeek API 返回为空");
            }
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            String result = content.asText("");
            if (result.isBlank()) {
                throw new RuntimeException("DeepSeek API 返回内容为空，请检查模型名称和 API Key 是否有效");
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API 调用失败: " + e.getMessage());
        }
    }
}
