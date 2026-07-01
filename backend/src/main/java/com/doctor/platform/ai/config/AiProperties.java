package com.doctor.platform.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "platform.ai")
public class AiProperties {

    private String provider;
    private String modelName;
    private String apiKey;
    private String baseUrl;
    private Boolean enabled;
}
