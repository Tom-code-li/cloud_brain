package com.doctor.platform.examlab;

import com.doctor.platform.DoctorPlatformApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DoctorPlatformApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicalItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsExamItemsFromMedicalItemTable() throws Exception {
        mockMvc.perform(get("/api/exam-items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].code").value("EXAM-CXR"))
            .andExpect(jsonPath("$.data[0].name").value("胸部DR正位片"))
            .andExpect(jsonPath("$.data[0].feeType").value("检查费"));
    }

    @Test
    void listsLabItemsFromMedicalItemTableWithKeyword() throws Exception {
        mockMvc.perform(get("/api/lab-items").param("keyword", "血"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].code").value("LAB-CBC"))
            .andExpect(jsonPath("$.data[0].name").value("血常规"))
            .andExpect(jsonPath("$.data[0].feeType").value("检验费"));
    }
}
