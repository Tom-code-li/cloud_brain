package com.hospital.medicalexam;

import com.hospital.medicalexam.common.R;
import com.hospital.medicalexam.controller.MedicalExamController;
import com.hospital.medicalexam.domain.dto.OrderExecuteRequest;
import com.hospital.medicalexam.domain.dto.ReportDraftRequest;
import com.hospital.medicalexam.domain.dto.ReportPublishRequest;
import com.hospital.medicalexam.domain.view.ExamLabReportView;
import com.hospital.medicalexam.domain.view.ExamLabTaskView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchResponse;
import com.hospital.medicalexam.service.MedicalExamWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MedicalExamControllerTest {
    @Autowired
    private MedicalExamController controller;
    @Autowired
    private MedicalExamWorkflowService service;

    @Test
    void pendingReturnsOkResponse() {
        R<List<ExamLabTaskView>> response = controller.pending(3L, "EXAM");

        assertThat(response.getCode()).isZero();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void publishWithDoctorHeadersReturnsConfirmedReport() {
        controller.executeOrder(3L, 3L, "EXAM", new OrderExecuteRequest(101L));
        R<ExamLabReportView> draftResponse = controller.createDraft(
                3L,
                3L,
                "EXAM",
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", "")
        );

        R<ExamLabReportView> response = controller.publish(
                3L,
                3L,
                "EXAM",
                new ReportPublishRequest(draftResponse.getData().reportId(), "考虑支气管炎可能")
        );

        assertThat(response.getCode()).isZero();
        assertThat(response.getData().status()).isEqualTo("已发布");
        assertThat(service.detail(response.getData().reportId()).conclusion()).isEqualTo("考虑支气管炎可能");
    }

    @Test
    void workbenchReturnsFilteredListAndStats() {
        controller.executeOrder(4L, 4L, "LAB", new OrderExecuteRequest(102L));

        R<ExamLabWorkbenchResponse> response = controller.workbench(4L, "LAB", "progress", "张晓雨", null, null);

        assertThat(response.getCode()).isZero();
        assertThat(response.getData().stats().progressCount()).isEqualTo(1);
        assertThat(response.getData().items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.orderItemId()).isEqualTo(102L);
                    assertThat(item.workbenchStatus()).isEqualTo("progress");
                });
    }

    @Test
    void workbenchAcceptsPatientNameAsSearchKeyword() {
        controller.executeOrder(4L, 4L, "LAB", new OrderExecuteRequest(102L));

        R<ExamLabWorkbenchResponse> response = controller.workbench(4L, "LAB", "progress", null, "张晓雨", null);

        assertThat(response.getCode()).isZero();
        assertThat(response.getData().items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.orderItemId()).isEqualTo(102L);
                    assertThat(item.patientName()).isEqualTo("张晓雨");
                });
    }

    @Test
    void workbenchFiltersByItemName() {
        R<ExamLabWorkbenchResponse> response = controller.workbench(4L, "LAB", "all", null, null, "血常规");

        assertThat(response.getCode()).isZero();
        assertThat(response.getData().items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.orderItemId()).isEqualTo(102L);
                    assertThat(item.itemName()).contains("血常规");
                });
    }
}
