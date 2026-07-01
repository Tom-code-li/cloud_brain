package com.doctor.platform.ai;

import com.doctor.platform.ai.service.AiSuggestionClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AiSuggestionClientWiringTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void usesOnlyConcreteAiSuggestionClientComponentBean() {
        Map<String, AiSuggestionClient> beans = applicationContext.getBeansOfType(AiSuggestionClient.class);

        assertThat(beans).containsOnlyKeys("deepSeekAiSuggestionClient");
    }
}
