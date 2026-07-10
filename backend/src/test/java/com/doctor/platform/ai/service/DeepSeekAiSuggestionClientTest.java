package com.doctor.platform.ai.service;

import com.doctor.platform.ai.config.AiProperties;
import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.AiSceneCode;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;

class DeepSeekAiSuggestionClientTest {

    @Test
    void callsDeepSeekChatCompletionsAndParsesJsonContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiProperties properties = new AiProperties();
        properties.setProvider("deepseek");
        properties.setModelName("deepseek-v4-pro");
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://api.deepseek.com");

        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
            .andExpect(method(POST))
            .andExpect(header(AUTHORIZATION, "Bearer test-key"))
            .andExpect(jsonPath("$.model").value("deepseek-v4-pro"))
            .andExpect(jsonPath("$.response_format.type").value("json_object"))
            .andExpect(jsonPath("$.messages[0].role").value("system"))
            .andRespond(withSuccess("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"sceneCode\\":\\"OUTPATIENT_INITIAL_SUGGESTION\\",\\"backgroundSummary\\":\\"真实模型摘要\\",\\"diagnosisDraft\\":\\"真实模型诊断\\",\\"planDraft\\":\\"真实模型计划\\",\\"evidence\\":[\\"证据1\\"],\\"examSuggestions\\":[\\"血常规\\"],\\"drugSuggestions\\":[\\"按医嘱用药\\"],\\"riskFlags\\":[\\"过敏史复核\\"],\\"reportSummary\\":null}"
                      }
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        DeepSeekAiSuggestionClient client = new DeepSeekAiSuggestionClient(builder, new ObjectMapper(), properties);
        AiSuggestionRequest request = new AiSuggestionRequest();
        request.setSceneCode(AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION);
        request.setPatientId(1L);
        request.setCurrentChiefComplaint("咳嗽");
        Patient patient = new Patient();
        patient.setPatientName("张晓雨");

        AiDraftBlock block = client.generateSuggestion(request, patient, List.of(), List.of(), List.of());

        assertThat(block.getDiagnosisDraft()).isEqualTo("真实模型诊断");
        assertThat(block.getEvidence()).containsExactly("证据1");
        server.verify();
    }
}
