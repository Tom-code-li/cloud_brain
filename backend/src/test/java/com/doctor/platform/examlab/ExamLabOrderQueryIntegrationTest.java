package com.doctor.platform.examlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExamLabOrderQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listOrdersByVisitAndTypeReturnsSubmittedOrderWithItems() throws Exception {
        mockMvc.perform(get("/api/exam-lab-orders")
                .param("visitId", "1")
                .param("orderType", "检查")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].orderNo").exists())
            .andExpect(jsonPath("$.data[0].feeStatus").exists())
            .andExpect(jsonPath("$.data[0].status").isNotEmpty())
            .andExpect(jsonPath("$.data[0].items").isArray())
            .andExpect(jsonPath("$.data[0].items[0].itemName").isNotEmpty())
            .andExpect(jsonPath("$.data[0].items[0].status").isNotEmpty());
    }
}
