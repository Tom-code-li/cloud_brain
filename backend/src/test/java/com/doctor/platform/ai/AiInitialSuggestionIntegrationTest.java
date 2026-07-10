package com.doctor.platform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiInitialSuggestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void initialSuggestionReturnsPossibleDiagnosesExamRecommendationsAndNoDrugSuggestions() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/outpatient/suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sceneCode": "OUTPATIENT_INITIAL_SUGGESTION",
                      "patientId": 1,
                      "visitId": 1,
                      "recordId": 1,
                      "currentChiefComplaint": "咳嗽发热2天，发烧",
                      "currentPresentIllness": "体温最高38.3摄氏度，伴咽痛，无胸痛、气促。",
                      "currentPhysicalExam": "咽部充血，双肺呼吸音清，未闻及明显干湿啰音。",
                      "currentDiagnosis": ""
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");

        assertThat(data.path("diagnosisDraft").asText()).isNotBlank();
        assertThat(data.path("possibleDiagnoses").isArray()).isTrue();
        assertThat(data.path("possibleDiagnoses")).hasSizeGreaterThanOrEqualTo(2);
        assertThat(data.path("possibleDiagnoses").path(0).path("name").asText()).isNotBlank();
        assertThat(data.path("possibleDiagnoses").path(0).path("reason").asText()).isNotBlank();

        assertThat(data.path("examRecommendations").isArray()).isTrue();
        assertThat(data.path("examRecommendations")).hasSizeGreaterThanOrEqualTo(1);
        assertThat(data.path("examRecommendations").path(0).path("type").asText()).isIn("检查", "检验");
        assertThat(data.path("examRecommendations").path(0).path("name").asText()).isNotBlank();
        assertThat(data.path("examRecommendations").path(0).path("reason").asText()).isNotBlank();

        assertThat(data.path("drugSuggestions").isArray()).isTrue();
        assertThat(data.path("drugSuggestions")).isEmpty();
        assertThat(data.path("planDraft").asText()).isBlank();

        assertThat(data.path("evidence").toString())
            .contains("主诉")
            .contains("现病史")
            .contains("体格检查");
    }
}
