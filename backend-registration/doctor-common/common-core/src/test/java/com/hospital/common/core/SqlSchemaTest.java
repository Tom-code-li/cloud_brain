package com.hospital.common.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SqlSchemaTest {
    private static final Path SCHEMA_SQL = Path.of("../../sql/schema.sql");
    private static final Path SEED_SQL = Path.of("../../sql/seed.sql");

    @Test
    void schemaContainsAllRequiredTables() throws Exception {
        String schema = Files.readString(SCHEMA_SQL).toLowerCase();
        List<String> tables = List.of(
                "sys_role", "sys_user", "patient", "department", "doctor",
                "doctor_schedule", "ai_schedule_suggestion", "ai_consultation",
                "registration", "triage_record", "fee_order", "fee_order_item",
                "payment_record", "refund_record", "outpatient_visit", "medical_record",
                "medical_item", "exam_lab_order", "exam_lab_order_item", "exam_lab_report",
                "exam_result_feature", "lab_result_item",
                "prescription", "prescription_item", "drug", "pharmacy_dispense",
                "pharmacy_return", "drug_stock_record", "ai_call_log"
        );

        for (String table : tables) {
            assertThat(schema).contains("create table if not exists " + table);
        }
    }

    @Test
    void schemaContainsKeyColumnsForDemoWorkflow() throws Exception {
        String schema = Files.readString(SCHEMA_SQL).toLowerCase();

        assertThat(schema)
                .contains("parent_id")
                .contains("floor")
                .contains("phone varchar(30)")
                .contains("sort_order")
                .contains("source varchar(30)")
                .contains("called_at")
                .contains("finished_at")
                .contains("current_treatment")
                .contains("final_diagnosis")
                .contains("final_opinion")
                .contains("confirmed_doctor_id")
                .contains("confirmed_at")
                .contains("visit_id bigint")
                .contains("item_code varchar(50)")
                .contains("item_spec varchar(100)")
                .contains("exam_site varchar(255)")
                .contains("specimen_type varchar(50)")
                .contains("priority varchar(20)")
                .contains("collection_way varchar(50)")
                .contains("ai_schedule_suggestion_detail")
                .contains("exam_result_feature")
                .contains("lab_result_item")
                .contains("patient_no")
                .contains("doctor_no")
                .contains("medical_record")
                .contains("exam_lab_order")
                .contains("ai_draft")
                .contains("doctor_review")
                .contains("pharmacy_dispense")
                .contains("business_id");
    }

    @Test
    void seedContainsIntegratedDemoDataAndCompleteWorkflow() throws Exception {
        String seed = Files.readString(SEED_SQL);
        String normalizedSeed = seed.toLowerCase();

        assertThat(seed)
                .contains("SUPER_ADMIN")
                .contains("REGISTRATION_DOCTOR")
                .contains("PATIENT")
                .contains("admin_chen")
                .contains("doc_reg_lu")
                .contains("doc_out_zhou")
                .contains("SCHEDULE_AI")
                .contains("TRIAGE_AI");

        assertThat(normalizedSeed)
                .contains("ai_schedule_suggestion_detail")
                .contains("exam_result_feature")
                .contains("lab_result_item")
                .contains("exam_lab_order")
                .contains("prescription")
                .contains("pharmacy_dispense");

        Pattern nullBusinessIdFeeOrder = Pattern.compile(
                "'(?:exam_lab|prescription)'\\s*,\\s*null\\s*,\\s*\\d",
                Pattern.CASE_INSENSITIVE
        );
        assertThat(nullBusinessIdFeeOrder.matcher(seed).find()).isFalse();
    }
}
