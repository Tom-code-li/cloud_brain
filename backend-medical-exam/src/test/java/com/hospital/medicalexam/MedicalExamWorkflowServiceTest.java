package com.hospital.medicalexam;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.medicalexam.common.BusinessException;
import com.hospital.medicalexam.domain.dto.ExamResultSaveRequest;
import com.hospital.medicalexam.domain.dto.LabResultSaveRequest;
import com.hospital.medicalexam.domain.dto.ReportDraftRequest;
import com.hospital.medicalexam.domain.dto.ReportPublishRequest;
import com.hospital.medicalexam.domain.dto.ReportRejectRequest;
import com.hospital.medicalexam.domain.view.ExamLabReportView;
import com.hospital.medicalexam.domain.view.ExamLabTaskView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchItemView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchResponse;
import com.hospital.medicalexam.domain.view.ItemSchemaView;
import com.hospital.medicalexam.domain.view.OrderDetailView;
import com.hospital.medicalexam.entity.ExamLabOrderItemEntity;
import com.hospital.medicalexam.entity.ExamLabReportEntity;
import com.hospital.medicalexam.mapper.ExamLabOrderItemMapper;
import com.hospital.medicalexam.mapper.ExamLabOrderMapper;
import com.hospital.medicalexam.mapper.ExamLabReportMapper;
import com.hospital.medicalexam.mapper.OutpatientVisitMapper;
import com.hospital.medicalexam.service.MedicalExamWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MedicalExamWorkflowServiceTest {
    @Autowired
    private MedicalExamWorkflowService service;
    @Autowired
    private ExamLabOrderItemMapper orderItemMapper;
    @Autowired
    private ExamLabOrderMapper orderMapper;
    @Autowired
    private ExamLabReportMapper reportMapper;
    @Autowired
    private OutpatientVisitMapper outpatientVisitMapper;

    @Test
    void examDoctorCanSeeOnlyExamTasks() {
        List<ExamLabTaskView> tasks = service.pending(3L, "EXAM");

        assertThat(tasks).isNotEmpty();
        assertThat(tasks).allSatisfy(task -> assertThat(task.itemType()).isEqualTo("检查"));
    }

    @Test
    void labDoctorCanSeeOnlyLabTasks() {
        List<ExamLabTaskView> tasks = service.pending(4L, "LAB");

        assertThat(tasks).isNotEmpty();
        assertThat(tasks).allSatisfy(task -> assertThat(task.itemType()).isEqualTo("检验"));
    }

    @Test
    void pendingTasksSynchronizeVisitToWaitExamLab() {
        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("接诊中");

        service.pending(3L, "EXAM");

        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("待检查检验");
    }

    @Test
    void unpaidOrderCannotExecute() {
        assertThatThrownBy(() -> service.executeOrder(103L, 3L, 3L, "EXAM"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("检查项目未缴费，暂不可执行");
    }

    @Test
    void examDoctorCannotExecuteLabTask() {
        assertThatThrownBy(() -> service.executeOrder(102L, 3L, 3L, "EXAM"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("项目执行科室类型不匹配");
    }

    @Test
    void executeOrderUpdatesOnlyOrderStatusForWorkflowJudgement() {
        service.executeOrder(101L, 3L, 3L, "EXAM");

        assertThat(orderMapper.selectById(1L).getStatus()).isEqualTo("执行中");
        assertThat(orderItemMapper.selectById(101L).getStatus()).isEqualTo("待执行");
    }

    @Test
    void executeThenCreateDraftSavesAiDraft() {
        service.executeOrder(101L, 3L, 3L, "EXAM");

        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        assertThat(draft.status()).isEqualTo("草稿");
        assertThat(draft.findings()).isEqualTo("胸部DR示双肺纹理稍增多");
        assertThat(draft.aiDraft()).contains("AI辅助草稿", "胸部DR正位片");
    }

    @Test
    void createDraftGeneratesReportNoFromDailyRepSequence() {
        String todayPrefix = "REP" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        ExamLabReportEntity existing = new ExamLabReportEntity();
        existing.setOrderId(3L);
        existing.setOrderItemId(103L);
        existing.setPatientId(1L);
        existing.setReportDoctorId(3L);
        existing.setReportNo(todayPrefix + "0019");
        existing.setReportType("检查");
        existing.setStatus("草稿");
        reportMapper.insert(existing);

        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        assertThat(reportMapper.selectById(draft.reportId()).getReportNo()).isEqualTo(todayPrefix + "0020");
    }

    @Test
    void createDraftUsesOrderStatusInsteadOfOrderItemStatus() {
        service.executeOrder(102L, 4L, 4L, "LAB");
        ExamLabOrderItemEntity item = orderItemMapper.selectById(102L);
        item.setStatus("执行中");
        orderItemMapper.updateById(item);

        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(102L, "血常规提示白细胞轻度升高", ""),
                4L,
                4L,
                "LAB"
        );

        assertThat(orderMapper.selectById(2L).getStatus()).isEqualTo("执行中");
        assertThat(draft.status()).isEqualTo("草稿");
        assertThat(draft.findings()).isEqualTo("血常规提示白细胞轻度升高");
    }

    @Test
    void saveExamResultUsesOrderStatusInsteadOfOrderItemStatus() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabOrderItemEntity item = orderItemMapper.selectById(101L);
        item.setStatus("执行中");
        orderItemMapper.updateById(item);

        service.saveExamResult(
                new ExamResultSaveRequest(
                        101L,
                        "胸部CT",
                        Map.of("findings", List.of("双肺纹理增多"), "notes", "未见明显渗出")
                ),
                3L,
                3L,
                "EXAM"
        );

        ExamLabOrderItemEntity saved = orderItemMapper.selectById(101L);
        assertThat(orderMapper.selectById(1L).getStatus()).isEqualTo("执行中");
        assertThat(saved.getStatus()).isEqualTo("已执行");
        assertThat(saved.getResultSummary()).isEqualTo("检查结果已录入");
    }

    @Test
    void chestDrFilterAndSchemaAcceptPlainChestProjectionName() {
        orderItemMapper.update(null, new LambdaUpdateWrapper<ExamLabOrderItemEntity>()
                .eq(ExamLabOrderItemEntity::getOrderItemId, 101L)
                .set(ExamLabOrderItemEntity::getItemName, "胸部正位片"));

        ItemSchemaView schema = service.getItemSchema("胸部正位片");
        ExamLabWorkbenchResponse response = service.workbench(3L, "EXAM", "all", null, "胸部DR正位片");

        assertThat(schema.itemName()).isEqualTo("胸部正位片");
        assertThat(schema.schemaType()).isEqualTo("ct");
        assertThat(response.items()).extracting(ExamLabWorkbenchItemView::orderItemId)
                .contains(101L);
        assertThat(response.items()).filteredOn(item -> item.orderItemId().equals(101L))
                .singleElement()
                .satisfies(item -> assertThat(item.itemName()).isEqualTo("胸部正位片"));
    }

    @Test
    void createDraftUpdatesExistingDraftInsteadOfDuplicating() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView first = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        ExamLabReportView updated = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR复查未见明显活动性病灶", ""),
                3L,
                3L,
                "EXAM"
        );

        assertThat(updated.reportId()).isEqualTo(first.reportId());
        assertThat(updated.status()).isEqualTo("草稿");
        assertThat(updated.findings()).isEqualTo("胸部DR复查未见明显活动性病灶");
        assertThat(updated.aiDraft()).contains("胸部DR复查未见明显活动性病灶");
    }

    @Test
    void createDraftTreatsBlankReportStatusAsEditableDraft() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView first = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );
        ExamLabReportEntity report = reportMapper.selectById(first.reportId());
        report.setStatus("");
        reportMapper.updateById(report);

        ExamLabReportView updated = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR复查未见明显活动性病灶", ""),
                3L,
                3L,
                "EXAM"
        );

        assertThat(updated.reportId()).isEqualTo(first.reportId());
        assertThat(updated.status()).isEqualTo("草稿");
        assertThat(updated.findings()).isEqualTo("胸部DR复查未见明显活动性病灶");
    }

    @Test
    void labSchemaAndSaveResultAcceptRenalFunctionAlias() {
        ExamLabOrderItemEntity item = orderItemMapper.selectById(102L);
        item.setItemName("肾功能三项");
        orderItemMapper.updateById(item);

        ItemSchemaView schema = service.getItemSchema("肾功能三项");

        assertThat(schema.itemName()).isEqualTo("肾功能三项");
        assertThat(schema.schemaType()).isEqualTo("lab");
        assertThat(schema.fields()).extracting("key")
                .containsExactly("CR", "BUN", "UA");

        service.executeOrder(102L, 4L, 4L, "LAB");
        service.saveLabResult(
                new LabResultSaveRequest(
                        102L,
                        "肾功能三项",
                        Map.of("CR", "80", "BUN", "5.2", "UA", "360")
                ),
                4L,
                4L,
                "LAB"
        );

        OrderDetailView detail = service.getOrderItemDetail(102L);
        assertThat(detail.labResultItems()).hasSize(3);
    }

    @Test
    void rejectDraftClearsReportAndResultRowsForReentry() {
        service.executeOrder(102L, 4L, 4L, "LAB");
        service.saveLabResult(
                new LabResultSaveRequest(
                        102L,
                        "血常规",
                        Map.of("WBC", "6.2", "RBC", "4.6", "HGB", "138", "PLT", "215", "NEUT", "58")
                ),
                4L,
                4L,
                "LAB"
        );
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(102L, "血常规指标均在参考范围内", ""),
                4L,
                4L,
                "LAB"
        );

        service.rejectReport(new ReportRejectRequest(102L), 4L, 4L, "LAB");

        OrderDetailView detail = service.getOrderItemDetail(102L);
        ExamLabOrderItemEntity item = orderItemMapper.selectById(102L);
        assertThat(reportMapper.selectById(draft.reportId())).isNull();
        assertThat(detail.reportId()).isNull();
        assertThat(detail.labResultItems()).isEmpty();
        assertThat(item.getStatus()).isEqualTo("已执行");
        assertThat(item.getResultSummary()).isNull();
    }

    @Test
    void rejectReportBlocksTimestampedDraftAsAlreadyPublished() {
        service.executeOrder(102L, 4L, 4L, "LAB");
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(102L, "血常规指标均在参考范围内", ""),
                4L,
                4L,
                "LAB"
        );
        ExamLabReportEntity report = reportMapper.selectById(draft.reportId());
        report.setPublishedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        assertThatThrownBy(() -> service.rejectReport(new ReportRejectRequest(102L), 4L, 4L, "LAB"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报告已发布或已回阅，无法退回重新录入");
    }

    @Test
    void publishDraftCompletesOrderItem() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        ExamLabReportView published = service.publish(
                new ReportPublishRequest(draft.reportId(), "考虑支气管炎可能"),
                3L,
                3L,
                "EXAM"
        );

        assertThat(published.status()).isEqualTo("已发布");
        assertThat(published.conclusion()).isEqualTo("考虑支气管炎可能");
        assertThat(published.doctorReview()).isEmpty();
        assertThat(service.pending(3L, "EXAM"))
                .noneMatch(task -> task.orderItemId().equals(101L));
    }

    @Test
    void workflowSynchronizesOutpatientVisitStatus() {
        service.pending(3L, "EXAM");
        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("待检查检验");

        service.executeOrder(101L, 3L, 3L, "EXAM");
        assertThat(orderMapper.selectById(1L).getStatus()).isEqualTo("执行中");

        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("检查检验中");

        ExamLabReportView examDraft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );
        service.publish(
                new ReportPublishRequest(examDraft.reportId(), "考虑支气管炎可能"),
                3L,
                3L,
                "EXAM"
        );
        assertThat(orderMapper.selectById(1L).getStatus()).isEqualTo("已完成");

        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("检查检验中");

        service.executeOrder(102L, 4L, 4L, "LAB");
        ExamLabReportView labDraft = service.createDraft(
                new ReportDraftRequest(102L, "血常规提示白细胞轻度升高", ""),
                4L,
                4L,
                "LAB"
        );
        service.publish(
                new ReportPublishRequest(labDraft.reportId(), "提示感染或炎症反应"),
                4L,
                4L,
                "LAB"
        );
        assertThat(orderMapper.selectById(2L).getStatus()).isEqualTo("已完成");

        assertThat(outpatientVisitMapper.selectById(1L).getStatus()).isEqualTo("报告待回阅");
    }

    @Test
    void reportsByRecordReturnsSavedReport() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        List<ExamLabReportView> reports = service.reportsByRecord(1L);

        assertThat(reports).extracting(ExamLabReportView::reportId)
                .contains(draft.reportId());
    }

    @Test
    void workbenchIncludesPendingProgressAndPublishedStats() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        ExamLabReportView draft = service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );
        service.publish(
                new ReportPublishRequest(draft.reportId(), "考虑支气管炎可能"),
                3L,
                3L,
                "EXAM"
        );

        ExamLabWorkbenchResponse response = service.workbench(3L, "EXAM", "all", null);

        assertThat(response.stats().allCount()).isEqualTo(1);
        assertThat(response.stats().pendingCount()).isZero();
        assertThat(response.stats().progressCount()).isZero();
        assertThat(response.stats().publishedCount()).isEqualTo(1);
        assertThat(response.stats().activeCount()).isZero();
        assertThat(response.items()).extracting(ExamLabWorkbenchItemView::orderItemId)
                .containsExactly(101L)
                .doesNotContain(103L, 104L);
        assertThat(response.items()).filteredOn(item -> item.orderItemId().equals(101L))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.workbenchStatus()).isEqualTo("published");
                    assertThat(item.birthday()).isEqualTo(LocalDate.of(1992, 4, 18));
                    assertThat(item.applyDoctorName()).isEqualTo("王门诊");
                    assertThat(item.executeDeptName()).isEqualTo("医学影像科");
                    assertThat(item.reportId()).isEqualTo(draft.reportId());
                    assertThat(item.reportStatus()).isEqualTo("已发布");
                });
    }

    @Test
    void workbenchStatusUsesOrderStatusOnly() {
        service.executeOrder(101L, 3L, 3L, "EXAM");
        service.createDraft(
                new ReportDraftRequest(101L, "胸部DR示双肺纹理稍增多", ""),
                3L,
                3L,
                "EXAM"
        );

        ExamLabWorkbenchResponse response = service.workbench(3L, "EXAM", "progress", null);

        assertThat(response.stats().allCount()).isEqualTo(1);
        assertThat(response.stats().progressCount()).isEqualTo(1);
        assertThat(response.items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.orderStatus()).isEqualTo("执行中");
                    assertThat(item.itemStatus()).isEqualTo("待执行");
                    assertThat(item.reportStatus()).isEqualTo("草稿");
                    assertThat(item.workbenchStatus()).isEqualTo("progress");
                });
    }

    @Test
    void workbenchFiltersByStatusAndKeyword() {
        service.executeOrder(102L, 4L, 4L, "LAB");

        ExamLabWorkbenchResponse response = service.workbench(4L, "LAB", "progress", "张晓雨");

        assertThat(response.stats().allCount()).isEqualTo(1);
        assertThat(response.stats().pendingCount()).isZero();
        assertThat(response.stats().progressCount()).isEqualTo(1);
        assertThat(response.stats().publishedCount()).isZero();
        assertThat(response.items()).singleElement()
                .satisfies(item -> {
                    assertThat(item.orderItemId()).isEqualTo(102L);
                    assertThat(item.patientName()).isEqualTo("张晓雨");
                    assertThat(item.workbenchStatus()).isEqualTo("progress");
                });
    }
}
