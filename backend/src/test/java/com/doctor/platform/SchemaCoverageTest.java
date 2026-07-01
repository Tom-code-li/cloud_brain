package com.doctor.platform;

import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaCoverageTest {

    private static final Map<String, String> ENTITY_CLASS_BY_TABLE = Map.ofEntries(
        Map.entry("sys_role", "com.doctor.platform.auth.entity.SysRole"),
        Map.entry("sys_user", "com.doctor.platform.auth.entity.SysUser"),
        Map.entry("patient", "com.doctor.platform.modules.outpatient.entity.Patient"),
        Map.entry("department", "com.doctor.platform.auth.entity.Department"),
        Map.entry("doctor", "com.doctor.platform.auth.entity.Doctor"),
        Map.entry("doctor_schedule", "com.doctor.platform.schedule.entity.DoctorSchedule"),
        Map.entry("ai_schedule_suggestion", "com.doctor.platform.ai.entity.AiScheduleSuggestion"),
        Map.entry("ai_consultation", "com.doctor.platform.ai.entity.AiConsultation"),
        Map.entry("registration", "com.doctor.platform.modules.outpatient.entity.Registration"),
        Map.entry("triage_record", "com.doctor.platform.triage.entity.TriageRecord"),
        Map.entry("fee_order", "com.doctor.platform.fee.entity.FeeOrder"),
        Map.entry("fee_order_item", "com.doctor.platform.fee.entity.FeeOrderItem"),
        Map.entry("payment_record", "com.doctor.platform.fee.entity.PaymentRecord"),
        Map.entry("refund_record", "com.doctor.platform.fee.entity.RefundRecord"),
        Map.entry("outpatient_visit", "com.doctor.platform.modules.outpatient.entity.OutpatientVisit"),
        Map.entry("medical_record", "com.doctor.platform.modules.outpatient.entity.MedicalRecord"),
        Map.entry("medical_item", "com.doctor.platform.examlab.entity.MedicalItem"),
        Map.entry("exam_lab_order", "com.doctor.platform.examlab.entity.ExamLabOrder"),
        Map.entry("exam_lab_order_item", "com.doctor.platform.examlab.entity.ExamLabOrderItem"),
        Map.entry("exam_lab_report", "com.doctor.platform.examlab.entity.ExamLabReport"),
        Map.entry("exam_result_feature", "com.doctor.platform.examlab.entity.ExamResultFeature"),
        Map.entry("lab_result_item", "com.doctor.platform.examlab.entity.LabResultItem"),
        Map.entry("drug", "com.doctor.platform.pharmacy.entity.Drug"),
        Map.entry("prescription", "com.doctor.platform.prescription.entity.Prescription"),
        Map.entry("prescription_item", "com.doctor.platform.prescription.entity.PrescriptionItem"),
        Map.entry("pharmacy_dispense", "com.doctor.platform.pharmacy.entity.PharmacyDispense"),
        Map.entry("pharmacy_return", "com.doctor.platform.pharmacy.entity.PharmacyReturn"),
        Map.entry("drug_stock_record", "com.doctor.platform.pharmacy.entity.DrugStockRecord"),
        Map.entry("ai_role_config", "com.doctor.platform.ai.entity.AiRoleConfig"),
        Map.entry("ai_prompt_template", "com.doctor.platform.ai.entity.AiPromptTemplate"),
        Map.entry("ai_call_log", "com.doctor.platform.ai.entity.AiCallLog")
    );

    private static final Map<String, String> MAPPER_CLASS_BY_TABLE = Map.ofEntries(
        Map.entry("sys_role", "com.doctor.platform.auth.mapper.SysRoleMapper"),
        Map.entry("sys_user", "com.doctor.platform.auth.mapper.SysUserMapper"),
        Map.entry("patient", "com.doctor.platform.modules.outpatient.mapper.PatientMapper"),
        Map.entry("department", "com.doctor.platform.auth.mapper.DepartmentMapper"),
        Map.entry("doctor", "com.doctor.platform.auth.mapper.DoctorMapper"),
        Map.entry("doctor_schedule", "com.doctor.platform.schedule.mapper.DoctorScheduleMapper"),
        Map.entry("ai_schedule_suggestion", "com.doctor.platform.ai.mapper.AiScheduleSuggestionMapper"),
        Map.entry("ai_consultation", "com.doctor.platform.ai.mapper.AiConsultationMapper"),
        Map.entry("registration", "com.doctor.platform.modules.outpatient.mapper.RegistrationMapper"),
        Map.entry("triage_record", "com.doctor.platform.triage.mapper.TriageRecordMapper"),
        Map.entry("fee_order", "com.doctor.platform.fee.mapper.FeeOrderMapper"),
        Map.entry("fee_order_item", "com.doctor.platform.fee.mapper.FeeOrderItemMapper"),
        Map.entry("payment_record", "com.doctor.platform.fee.mapper.PaymentRecordMapper"),
        Map.entry("refund_record", "com.doctor.platform.fee.mapper.RefundRecordMapper"),
        Map.entry("outpatient_visit", "com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper"),
        Map.entry("medical_record", "com.doctor.platform.modules.outpatient.mapper.MedicalRecordMapper"),
        Map.entry("medical_item", "com.doctor.platform.examlab.mapper.MedicalItemMapper"),
        Map.entry("exam_lab_order", "com.doctor.platform.examlab.mapper.ExamLabOrderMapper"),
        Map.entry("exam_lab_order_item", "com.doctor.platform.examlab.mapper.ExamLabOrderItemMapper"),
        Map.entry("exam_lab_report", "com.doctor.platform.examlab.mapper.ExamLabReportMapper"),
        Map.entry("exam_result_feature", "com.doctor.platform.examlab.mapper.ExamResultFeatureMapper"),
        Map.entry("lab_result_item", "com.doctor.platform.examlab.mapper.LabResultItemMapper"),
        Map.entry("drug", "com.doctor.platform.pharmacy.mapper.DrugMapper"),
        Map.entry("prescription", "com.doctor.platform.prescription.mapper.PrescriptionMapper"),
        Map.entry("prescription_item", "com.doctor.platform.prescription.mapper.PrescriptionItemMapper"),
        Map.entry("pharmacy_dispense", "com.doctor.platform.pharmacy.mapper.PharmacyDispenseMapper"),
        Map.entry("pharmacy_return", "com.doctor.platform.pharmacy.mapper.PharmacyReturnMapper"),
        Map.entry("drug_stock_record", "com.doctor.platform.pharmacy.mapper.DrugStockRecordMapper"),
        Map.entry("ai_role_config", "com.doctor.platform.ai.mapper.AiRoleConfigMapper"),
        Map.entry("ai_prompt_template", "com.doctor.platform.ai.mapper.AiPromptTemplateMapper"),
        Map.entry("ai_call_log", "com.doctor.platform.ai.mapper.AiCallLogMapper")
    );

    @Test
    void everySchemaTableHasEntityWithMatchingTableName() throws Exception {
        for (Map.Entry<String, String> entry : ENTITY_CLASS_BY_TABLE.entrySet()) {
            Class<?> entityClass = Class.forName(entry.getValue());
            TableName tableName = entityClass.getAnnotation(TableName.class);
            assertThat(tableName)
                .as(entry.getValue() + " should declare @TableName")
                .isNotNull();
            assertThat(tableName.value()).isEqualTo(entry.getKey());
        }
    }

    @Test
    void everySchemaTableHasMapper() throws Exception {
        for (String mapperClassName : MAPPER_CLASS_BY_TABLE.values()) {
            assertThat(Class.forName(mapperClassName))
                .as(mapperClassName + " should exist")
                .isNotNull();
        }
    }
}
