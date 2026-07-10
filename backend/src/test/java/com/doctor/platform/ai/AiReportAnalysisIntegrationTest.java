package com.doctor.platform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiReportAnalysisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postReportSuggestionUsesPersistedExamLabReportResults() throws Exception {
        insertCompletedReport();

        MvcResult result = mockMvc.perform(post("/api/ai/outpatient/suggestions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sceneCode": "OUTPATIENT_POST_REPORT_SUGGESTION",
                      "patientId": 1,
                      "visitId": 1,
                      "orderId": 101,
                      "reportId": 101
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");

        assertThat(data.path("reportSummary").asText())
            .contains("右肺中叶斑片状高密度影")
            .contains("白细胞计数 12.5");
        assertThat(data.path("evidence").toString())
            .contains("胸部DR正位片")
            .contains("社区获得性肺炎可能");
    }

    private void insertCompletedReport() {
        jdbcTemplate.update("""
            INSERT INTO exam_lab_order(
              order_id, order_no, visit_id, record_id, patient_id, apply_doctor_id, execute_dept_id,
              order_type, clinical_diagnosis, purpose, total_amount, fee_status, status, applied_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            101L,
            "EXL-AI-REPORT-101",
            1L,
            1L,
            1L,
            1L,
            1L,
            "检查",
            "社区获得性肺炎待排",
            "排查肺部感染",
            new BigDecimal("28.00"),
            "已支付",
            "已完成"
        );
        jdbcTemplate.update("""
            INSERT INTO exam_lab_order_item(
              order_item_id, order_id, item_id, item_name, item_type, unit_price,
              quantity, amount, status, executed_at, result_summary
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
            """,
            101L,
            101L,
            1L,
            "胸部DR正位片",
            "检查",
            new BigDecimal("28.00"),
            BigDecimal.ONE,
            new BigDecimal("28.00"),
            "已完成",
            "右肺中叶炎性改变"
        );
        jdbcTemplate.update("""
            INSERT INTO exam_lab_report(
              report_id, order_id, order_item_id, patient_id, report_doctor_id,
              report_no, report_type, findings, conclusion, doctor_review, status, published_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            101L,
            101L,
            101L,
            1L,
            1L,
            "REP-AI-REPORT-101",
            "检查",
            "胸片显示右肺中叶斑片状高密度影；检验回报白细胞计数 12.5，中性粒细胞 85%。",
            "结合影像及炎症指标，社区获得性肺炎可能。",
            "建议结合症状、体征和药物过敏史综合判断。",
            "已发布"
        );
    }
}
