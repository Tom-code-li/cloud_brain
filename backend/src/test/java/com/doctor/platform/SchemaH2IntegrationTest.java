package com.doctor.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchemaH2IntegrationTest {

    private static final List<String> SCHEMA_TABLES = List.of(
        "sys_role",
        "sys_user",
        "patient",
        "department",
        "doctor",
        "doctor_schedule",
        "ai_schedule_suggestion",
        "ai_consultation",
        "registration",
        "triage_record",
        "fee_order",
        "fee_order_item",
        "payment_record",
        "refund_record",
        "outpatient_visit",
        "medical_record",
        "medical_item",
        "exam_lab_order",
        "exam_lab_order_item",
        "exam_lab_report",
        "exam_result_feature",
        "lab_result_item",
        "drug",
        "prescription",
        "prescription_item",
        "pharmacy_dispense",
        "pharmacy_return",
        "drug_stock_record",
        "ai_role_config",
        "ai_prompt_template",
        "ai_call_log"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void h2TestSchemaCreatesEveryMysqlSchemaTable() {
        for (String tableName : SCHEMA_TABLES) {
            Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where lower(table_name) = ?",
                Integer.class,
                tableName
            );
            assertThat(count)
                .as("H2 schema should create table " + tableName)
                .isEqualTo(1);
        }
    }

    @Test
    void h2SchemaIncludesNewOutpatientPersistenceColumns() {
        assertThat(columnExists("medical_record", "current_treatment")).isTrue();
        assertThat(columnExists("medical_record", "final_diagnosis")).isTrue();
        assertThat(columnExists("medical_record", "final_opinion")).isTrue();
        assertThat(columnExists("medical_record", "confirmed_doctor_id")).isTrue();
        assertThat(columnExists("medical_record", "confirmed_at")).isTrue();

        assertThat(columnExists("exam_lab_order", "exam_site")).isTrue();
        assertThat(columnExists("exam_lab_order", "specimen_type")).isTrue();
        assertThat(columnExists("exam_lab_order", "remark")).isTrue();
        assertThat(columnExists("exam_lab_order", "priority")).isTrue();
        assertThat(columnExists("exam_lab_order", "collection_way")).isTrue();

        assertThat(columnExists("fee_order", "visit_id")).isTrue();
        assertThat(columnExists("fee_order_item", "item_code")).isTrue();
        assertThat(columnExists("fee_order_item", "item_spec")).isTrue();
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where lower(table_name) = ? and lower(column_name) = ?",
            Integer.class,
            tableName.toLowerCase(),
            columnName.toLowerCase()
        );
        return count != null && count > 0;
    }
}
