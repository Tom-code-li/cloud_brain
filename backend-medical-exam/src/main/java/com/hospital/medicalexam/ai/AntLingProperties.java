package com.hospital.medicalexam.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.ant-ling")
public record AntLingProperties(
        String apiKey,
        String baseUrl,
        String model,
        Integer timeoutSeconds
) {
}
