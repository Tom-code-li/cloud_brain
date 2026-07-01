package com.doctor.platform.examlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExamLabReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsPersistedExamLabReportsForVisit() throws Exception {
        mockMvc.perform(get("/api/exam-lab-reports")
                .param("patientId", "1")
                .param("visitId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].itemName").value("胸部DR正位片"))
            .andExpect(jsonPath("$.data[0].findings").value(containsString("右肺中叶斑片状高密度影")))
            .andExpect(jsonPath("$.data[0].conclusion").value(containsString("社区获得性肺炎可能")))
            .andExpect(jsonPath("$.data[0].examFeatures").isArray())
            .andExpect(jsonPath("$.data[0].labResultItems").isArray());
    }
}
