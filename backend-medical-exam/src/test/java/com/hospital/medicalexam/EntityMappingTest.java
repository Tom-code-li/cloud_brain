package com.hospital.medicalexam;

import com.hospital.medicalexam.entity.ExamLabReportEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMappingTest {
    @Test
    void reportEntityStoresExistingTableColumns() {
        ExamLabReportEntity entity = new ExamLabReportEntity();
        entity.setReportId(1L);
        entity.setOrderId(2L);
        entity.setOrderItemId(3L);
        entity.setPatientId(4L);
        entity.setReportDoctorId(5L);
        entity.setReportNo("REP202606240001");
        entity.setReportType("检查");
        entity.setFindings("findings");
        entity.setConclusion("conclusion");
        entity.setAiDraft("ai");
        entity.setDoctorReview("review");
        entity.setStatus("草稿");

        assertThat(entity.getReportId()).isEqualTo(1L);
        assertThat(entity.getOrderItemId()).isEqualTo(3L);
        assertThat(entity.getAiDraft()).isEqualTo("ai");
    }
}
