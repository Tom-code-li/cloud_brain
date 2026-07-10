package com.doctor.platform.outpatient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.examlab.dto.ExamLabRequestItem;
import com.doctor.platform.examlab.dto.ExamLabRequestForm;
import com.doctor.platform.examlab.dto.FrontendExamLabRequest;
import com.doctor.platform.examlab.dto.ReportReviewRequest;
import com.doctor.platform.examlab.service.ExamLabOrderService;
import com.doctor.platform.examlab.service.ExamLabReportService;
import com.doctor.platform.modules.outpatient.dto.EncounterStartRequest;
import com.doctor.platform.modules.outpatient.dto.FinalDiagnosisSaveRequest;
import com.doctor.platform.modules.outpatient.dto.MedicalRecordSaveRequest;
import com.doctor.platform.modules.outpatient.entity.MedicalRecord;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.MedicalRecordMapper;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import com.doctor.platform.modules.outpatient.service.OutpatientWorkbenchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OutpatientStatusFlowIntegrationTest {

    @Autowired
    private OutpatientWorkbenchService outpatientWorkbenchService;

    @Autowired
    private ExamLabOrderService examLabOrderService;

    @Autowired
    private ExamLabReportService examLabReportService;

    @Autowired
    private OutpatientVisitMapper outpatientVisitMapper;

    @Autowired
    private MedicalRecordMapper medicalRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savingMedicalRecordMovesVisitToInProgress() {
        MedicalRecordSaveRequest request = new MedicalRecordSaveRequest();
        request.setPatientId(2L);
        request.setVisitId(2L);
        request.setChiefComplaint("头痛1天");
        request.setPresentIllness("今日出现头痛不适。");
        request.setStatus("初诊暂存");

        outpatientWorkbenchService.saveMedicalRecord(request);

        OutpatientVisit visit = outpatientVisitMapper.selectById(2L);
        assertThat(visit.getStatus()).isEqualTo("接诊中");
    }

    @Test
    void submittingExamRequestMovesVisitToPendingExamLab() {
        FrontendExamLabRequest request = new FrontendExamLabRequest();
        request.setPatientId(1L);
        request.setVisitId(1L);
        request.setRecordId(1L);

        ExamLabRequestItem item = new ExamLabRequestItem();
        item.setId(1L);
        item.setCode("EXAM-CXR");
        item.setName("胸部DR正位片");
        item.setPrice(new BigDecimal("28.00"));
        item.setQuantity(BigDecimal.ONE);
        request.setExamItems(List.of(item));

        ExamLabRequestForm form = new ExamLabRequestForm();
        form.setPurpose("排查肺部感染");
        form.setSite("胸部");
        form.setNotes("门诊检查");
        request.setForm(form);

        examLabOrderService.saveFrontendRequest(request, "检查");

        OutpatientVisit visit = outpatientVisitMapper.selectById(1L);
        assertThat(visit.getStatus()).isEqualTo("待检查检验");
        String orderStatus = jdbcTemplate.queryForObject(
            "select status from exam_lab_order where visit_id = ? order by order_id desc limit 1",
            String.class,
            1L
        );
        assertThat(orderStatus).isEqualTo("待执行");
    }

    @Test
    void finalDiagnosisMovesVisitToPendingDisposalWhenFeeExists() {
        outpatientVisitMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutpatientVisit>()
                .eq(OutpatientVisit::getVisitId, 1L)
                .set(OutpatientVisit::getStatus, "待确诊"));

        jdbcTemplate.update("""
            insert into fee_order(order_no, patient_id, registration_id, visit_id, business_type, business_id, total_amount, paid_amount, refund_amount, status, created_by, created_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            "FEE-STATUS-FLOW-1", 1L, 1L, 1L, "PRESCRIPTION", 9999L, new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, "待支付", 1L
        );

        FinalDiagnosisSaveRequest request = new FinalDiagnosisSaveRequest();
        request.setPatientId(1L);
        request.setVisitId(1L);
        request.setRecordId(1L);
        request.setFinalDiagnosis("社区获得性肺炎");
        request.setFinalOpinion("建议抗感染治疗");

        outpatientWorkbenchService.saveFinalDiagnosis(request);

        OutpatientVisit visit = outpatientVisitMapper.selectOne(
            new LambdaQueryWrapper<OutpatientVisit>().eq(OutpatientVisit::getVisitId, 1L)
        );
        assertThat(visit.getStatus()).isEqualTo("待处置");
    }

    @Test
    void reviewingReportMovesVisitToPendingDiagnosis() {
        outpatientVisitMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutpatientVisit>()
                .eq(OutpatientVisit::getVisitId, 1L)
                .set(OutpatientVisit::getStatus, "报告待回阅"));

        ReportReviewRequest request = new ReportReviewRequest();
        request.setPatientId(1L);
        request.setVisitId(1L);
        request.setReportId(1L);

        examLabReportService.markReportReviewed(request);

        OutpatientVisit visit = outpatientVisitMapper.selectById(1L);
        assertThat(visit.getStatus()).isEqualTo("待确诊");
    }

    @Test
    void savingMedicalRecordRejectsStatusOutsideDatabaseEnum() {
        MedicalRecordSaveRequest request = new MedicalRecordSaveRequest();
        request.setPatientId(1L);
        request.setVisitId(1L);
        request.setStatus("处理中");

        assertThatThrownBy(() -> outpatientWorkbenchService.saveMedicalRecord(request))
            .hasMessage("病历状态非法，仅允许：初诊暂存、待补充、已完成、已作废");
    }

    @Test
    void startingEncounterCreatesMedicalRecordIfMissingAndDoesNotDuplicate() {
        jdbcTemplate.update("delete from medical_record where visit_id = ?", 2L);

        MedicalRecord existing = medicalRecordMapper.selectOne(
            new LambdaQueryWrapper<MedicalRecord>().eq(MedicalRecord::getVisitId, 2L)
        );
        assertThat(existing).isNull();

        EncounterStartRequest request = new EncounterStartRequest();
        request.setVisitId(2L);

        outpatientWorkbenchService.startEncounter(request);
        outpatientWorkbenchService.startEncounter(request);

        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from medical_record where visit_id = ?",
            Integer.class,
            2L
        );
        String recordStatus = jdbcTemplate.queryForObject(
            "select status from medical_record where visit_id = ?",
            String.class,
            2L
        );
        OutpatientVisit visit = outpatientVisitMapper.selectById(2L);

        assertThat(count).isEqualTo(1);
        assertThat(recordStatus).isEqualTo("初诊暂存");
        assertThat(visit.getStatus()).isEqualTo("接诊中");
    }

    @Test
    void creatingPrescriptionMovesVisitToPendingDisposal() {
        jdbcTemplate.update("""
            insert into prescription(
              prescription_no, visit_id, record_id, patient_id, doctor_id, total_amount, fee_status, audit_status, status, diagnosis, usage_note, created_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """,
            "PRE-VISIT-FLOW-1", 1L, 1L, 1L, 1L, new BigDecimal("25.60"), "待支付", "审核通过", "待缴费", "社区获得性肺炎", "按医嘱服用"
        );

        outpatientVisitMapper.update(null,
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<OutpatientVisit>()
                .eq(OutpatientVisit::getVisitId, 1L)
                .set(OutpatientVisit::getStatus, "待确诊"));

        FinalDiagnosisSaveRequest request = new FinalDiagnosisSaveRequest();
        request.setPatientId(1L);
        request.setVisitId(1L);
        request.setRecordId(1L);
        request.setFinalDiagnosis("社区获得性肺炎");
        request.setFinalOpinion("建议门诊抗感染治疗");

        outpatientWorkbenchService.saveFinalDiagnosis(request);

        OutpatientVisit visit = outpatientVisitMapper.selectById(1L);
        assertThat(visit.getStatus()).isEqualTo("待处置");
    }
}
