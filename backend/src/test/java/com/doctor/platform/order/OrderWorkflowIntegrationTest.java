package com.doctor.platform.order;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void examRequestCreatesOrderItemsAndFeeOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/exam-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "patientId": 1,
                      "visitId": 1,
                      "recordId": 1,
                      "examItems": [
                        {
                          "id": 1,
                          "code": "EXAM-CXR",
                          "name": "胸部DR正位片",
                          "price": 28.00,
                          "quantity": 1
                        }
                      ],
                      "form": {
                        "purpose": "排查肺部感染",
                        "site": "胸部",
                        "notes": "门诊检查"
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        long orderId = data.path("orderId").asLong();
        long feeOrderId = data.path("feeOrderId").asLong();

        assertThat(orderId).isPositive();
        assertThat(feeOrderId).isPositive();
        assertThat(queryDecimal("select total_amount from exam_lab_order where order_id = ?", orderId))
            .isEqualByComparingTo("28.00");
        assertThat(queryString("select exam_site from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("胸部");
        assertThat(queryString("select remark from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("门诊检查");
        assertThat(queryString("select fee_status from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("待支付");
        assertThat(queryString("select status from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("待执行");
        assertThat(queryInt("select count(*) from exam_lab_order_item where order_id = ? and item_id = ? and item_type = ?", orderId, 1L, "检查"))
            .isEqualTo(1);
        assertThat(queryString("select status from exam_lab_order_item where order_id = ? and item_id = ?", orderId, 1L))
            .isEqualTo("待执行");
        assertThat(queryDecimal("select total_amount from fee_order where fee_order_id = ? and business_type = ?", feeOrderId, "EXAM_LAB_ORDER"))
            .isEqualByComparingTo("28.00");
        assertThat(queryLong("select visit_id from fee_order where fee_order_id = ?", feeOrderId))
            .isEqualTo(1L);
        assertThat(queryInt("select count(*) from fee_order_item where fee_order_id = ? and item_name = ?", feeOrderId, "胸部DR正位片"))
            .isEqualTo(1);
        assertThat(queryString("select item_code from fee_order_item where fee_order_id = ? and item_name = ?", feeOrderId, "胸部DR正位片"))
            .isEqualTo("EXAM-CXR");
    }

    @Test
    void labRequestCreatesOrderItemsAndFeeOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/lab-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "patientId": 1,
                      "visitId": 1,
                      "recordId": 1,
                      "labItems": [
                        {
                          "id": 2,
                          "code": "LAB-CBC",
                          "name": "血常规",
                          "price": 18.50,
                          "quantity": 1
                        },
                        {
                          "id": 3,
                          "code": "LAB-CRP",
                          "name": "C反应蛋白",
                          "price": 26.00,
                          "quantity": 1
                        }
                      ],
                      "form": {
                        "purpose": "评估炎症指标",
                        "specimen": "静脉血",
                        "priority": "加急",
                        "collectionWay": "床旁采样"
                      }
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        long orderId = data.path("orderId").asLong();
        long feeOrderId = data.path("feeOrderId").asLong();

        assertThat(queryDecimal("select total_amount from exam_lab_order where order_id = ?", orderId))
            .isEqualByComparingTo("44.50");
        assertThat(queryString("select specimen_type from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("静脉血");
        assertThat(queryString("select fee_status from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("待支付");
        assertThat(queryString("select status from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("待执行");
        assertThat(queryString("select priority from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("加急");
        assertThat(queryString("select collection_way from exam_lab_order where order_id = ?", orderId))
            .isEqualTo("床旁采样");
        assertThat(queryInt("select count(*) from exam_lab_order_item where order_id = ? and item_type = ?", orderId, "检验"))
            .isEqualTo(2);
        assertThat(queryString("select status from exam_lab_order_item where order_id = ? and item_name = ?", orderId, "血常规"))
            .isEqualTo("待执行");
        assertThat(queryDecimal("select total_amount from fee_order where fee_order_id = ?", feeOrderId))
            .isEqualByComparingTo("44.50");
        assertThat(queryLong("select visit_id from fee_order where fee_order_id = ?", feeOrderId))
            .isEqualTo(1L);
        assertThat(queryInt("select count(*) from fee_order_item where fee_order_id = ?", feeOrderId))
            .isEqualTo(2);
    }

    @Test
    void prescriptionCreatesItemsAndFeeOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "patientId": 1,
                      "visitId": 1,
                      "recordId": 1,
                      "diagnosis": "社区获得性肺炎",
                      "usageNote": "按医嘱服用",
                      "items": [
                        {
                          "drugId": 1,
                          "code": "DRUG-AMOX",
                          "name": "阿莫西林胶囊",
                          "spec": "0.25g*24粒/盒",
                          "price": 12.80,
                          "quantity": 2,
                          "dosage": "0.5g",
                          "frequency": "每日三次",
                          "usageMethod": "口服",
                          "days": 3
                        }
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        long prescriptionId = data.path("prescriptionId").asLong();
        long feeOrderId = data.path("feeOrderId").asLong();

        assertThat(prescriptionId).isPositive();
        assertThat(feeOrderId).isPositive();
        assertThat(queryDecimal("select total_amount from prescription where prescription_id = ?", prescriptionId))
            .isEqualByComparingTo("25.60");
        assertThat(queryString("select fee_status from prescription where prescription_id = ?", prescriptionId))
            .isEqualTo("待支付");
        assertThat(queryString("select audit_status from prescription where prescription_id = ?", prescriptionId))
            .isEqualTo("审核通过");
        assertThat(queryString("select status from prescription where prescription_id = ?", prescriptionId))
            .isEqualTo("待缴费");
        assertThat(queryInt("select count(*) from prescription_item where prescription_id = ? and drug_id = ?", prescriptionId, 1L))
            .isEqualTo(1);
        assertThat(queryString("select status from prescription_item where prescription_id = ? and drug_id = ?", prescriptionId, 1L))
            .isEqualTo("待发药");
        assertThat(queryDecimal("select total_amount from fee_order where fee_order_id = ? and business_type = ?", feeOrderId, "PRESCRIPTION"))
            .isEqualByComparingTo("25.60");
        assertThat(queryLong("select visit_id from fee_order where fee_order_id = ?", feeOrderId))
            .isEqualTo(1L);
        assertThat(queryInt("select count(*) from fee_order_item where fee_order_id = ? and item_type = ?", feeOrderId, "药品"))
            .isEqualTo(1);
        assertThat(queryString("select item_spec from fee_order_item where fee_order_id = ? and item_type = ?", feeOrderId, "药品"))
            .isEqualTo("0.25g*24粒/盒");
    }

    private BigDecimal queryDecimal(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
    }

    private Integer queryInt(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

    private Long queryLong(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Long.class, args);
    }

    private String queryString(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, String.class, args);
    }
}
