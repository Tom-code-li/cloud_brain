package com.doctor.platform.ai;

import com.doctor.platform.ai.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "platform.ai.provider=deepseek",
    "platform.ai.model-name=deepseek-v4-pro",
    "platform.ai.api-key=test-key",
    "platform.ai.base-url=https://api.deepseek.com/v1",
    "platform.ai.enabled=true"
})
@ActiveProfiles("test")
class AiEnvConfigurationTest {

    @Autowired
    private AiProperties aiProperties;

    @Test
    void bindsAiPropertiesFromSpringConfigurationOverrides() {
        assertThat(aiProperties.getProvider()).isEqualTo("deepseek");
        assertThat(aiProperties.getModelName()).isEqualTo("deepseek-v4-pro");
        assertThat(aiProperties.getApiKey()).isEqualTo("test-key");
        assertThat(aiProperties.getBaseUrl()).isEqualTo("https://api.deepseek.com/v1");
        assertThat(aiProperties.getEnabled()).isTrue();
    }
}
