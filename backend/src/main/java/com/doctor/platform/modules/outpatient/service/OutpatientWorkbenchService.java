package com.doctor.platform.modules.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.modules.outpatient.dto.ExamLabSummaryResponse;
import com.doctor.platform.modules.outpatient.dto.FeeSummaryResponse;
import com.doctor.platform.modules.outpatient.dto.EncounterStartRequest;
import com.doctor.platform.modules.outpatient.dto.FinalDiagnosisSaveRequest;
import com.doctor.platform.modules.outpatient.dto.MedicalRecordResponse;
import com.doctor.platform.modules.outpatient.dto.MedicalRecordSaveRequest;
import com.doctor.platform.modules.outpatient.dto.PatientContextResponse;
import com.doctor.platform.modules.outpatient.dto.PatientListItemResponse;
import com.doctor.platform.modules.outpatient.dto.PrescriptionSummaryResponse;
import com.doctor.platform.modules.outpatient.dto.RegistrationSummaryResponse;
import com.doctor.platform.modules.outpatient.dto.SkipRequest;
import com.doctor.platform.modules.outpatient.dto.VisitSummaryResponse;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.doctor.platform.modules.outpatient.mapper.PatientMapper;
import com.doctor.platform.modules.outpatient.entity.MedicalRecord;
import com.doctor.platform.modules.outpatient.mapper.MedicalRecordMapper;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.examlab.mapper.ExamLabOrderMapper;
import com.doctor.platform.fee.entity.FeeOrder;
import com.doctor.platform.fee.mapper.FeeOrderMapper;
import com.doctor.platform.infrastructure.exception.BusinessException;
import com.doctor.platform.prescription.entity.Prescription;
import com.doctor.platform.prescription.mapper.PrescriptionMapper;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import com.doctor.platform.modules.outpatient.entity.Registration;
import com.doctor.platform.modules.outpatient.mapper.RegistrationMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class OutpatientWorkbenchService {

    private static final java.util.Set<String> ALLOWED_MEDICAL_RECORD_STATUSES =
        java.util.Set.of("初诊暂存", "待补充", "已完成", "已作废");

    private final PatientMapper patientMapper;
    private final OutpatientVisitMapper outpatientVisitMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final ExamLabOrderMapper examLabOrderMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final RegistrationMapper registrationMapper;
    private final FeeOrderMapper feeOrderMapper;

    public OutpatientWorkbenchService(PatientMapper patientMapper,
                                      OutpatientVisitMapper outpatientVisitMapper,
                                      MedicalRecordMapper medicalRecordMapper,
                                      ExamLabOrderMapper examLabOrderMapper,
                                      PrescriptionMapper prescriptionMapper,
                                      RegistrationMapper registrationMapper,
                                      FeeOrderMapper feeOrderMapper) {
        this.patientMapper = patientMapper;
        this.outpatientVisitMapper = outpatientVisitMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.examLabOrderMapper = examLabOrderMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.registrationMapper = registrationMapper;
        this.feeOrderMapper = feeOrderMapper;
    }

    public List<PatientListItemResponse> listPatients(String patientNo, String patientName, String visitStatus, String visitGroup) {
        LambdaQueryWrapper<Patient> patientQuery = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(patientNo)) {
            patientQuery.like(Patient::getPatientNo, patientNo.trim());
        }
        if (StringUtils.hasText(patientName)) {
            patientQuery.like(Patient::getPatientName, patientName.trim());
        }

        return patientMapper.selectList(patientQuery).stream().map(patient -> {
            OutpatientVisit visit = outpatientVisitMapper.selectOne(
                new LambdaQueryWrapper<OutpatientVisit>()
                    .eq(OutpatientVisit::getPatientId, patient.getPatientId())
                    .orderByDesc(OutpatientVisit::getCreatedAt)
                    .last("limit 1")
            );
            Registration registration = visit == null ? null : registrationMapper.selectById(visit.getRegistrationId());
            if (!matchesVisitFilter(visit, visitStatus, visitGroup)) {
                return null;
            }
            return PatientListItemResponse.builder()
                .patientId(patient.getPatientId())
                .visitId(visit != null ? visit.getVisitId() : null)
                .patientNo(patient.getPatientNo())
                .patientName(patient.getPatientName())
                .gender(patient.getGender())
                .idCard(patient.getIdCard())
                .age(calculateAge(patient.getBirthday()))
                .status(visit != null ? visit.getStatus() : "待接诊")
                .visitStatus(visit != null ? visit.getStatus() : "待接诊")
                .queueNo(visit != null ? visit.getQueueNo() : null)
                .registeredAt(registration != null ? registration.getRegisteredAt() : null)
                .build();
        }).filter(java.util.Objects::nonNull).toList();
    }

    public PatientContextResponse getPatientContext(Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        OutpatientVisit visit = outpatientVisitMapper.selectOne(
            new LambdaQueryWrapper<OutpatientVisit>()
                .eq(OutpatientVisit::getPatientId, patientId)
                .orderByDesc(OutpatientVisit::getCreatedAt)
                .last("limit 1")
        );
        Registration registration = visit == null ? null : registrationMapper.selectById(visit.getRegistrationId());

        MedicalRecord medicalRecord = visit == null ? null : medicalRecordMapper.selectOne(
            new LambdaQueryWrapper<MedicalRecord>().eq(MedicalRecord::getVisitId, visit.getVisitId())
        );

        List<ExamLabSummaryResponse> allOrders = visit == null ? List.of() : examLabOrderMapper.selectList(
            new LambdaQueryWrapper<ExamLabOrder>().eq(ExamLabOrder::getVisitId, visit.getVisitId())
        ).stream().map(order -> ExamLabSummaryResponse.builder()
            .orderId(order.getOrderId())
            .orderNo(order.getOrderNo())
            .orderType(order.getOrderType())
            .purpose(order.getPurpose())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .build()).toList();

        List<ExamLabSummaryResponse> examOrders = allOrders.stream()
            .filter(order -> "检查".equals(order.getOrderType()))
            .toList();

        List<ExamLabSummaryResponse> labOrders = allOrders.stream()
            .filter(order -> "检验".equals(order.getOrderType()))
            .toList();

        List<PrescriptionSummaryResponse> prescriptions = visit == null ? List.of() : prescriptionMapper.selectList(
            new LambdaQueryWrapper<Prescription>().eq(Prescription::getVisitId, visit.getVisitId())
        ).stream().map(item -> PrescriptionSummaryResponse.builder()
            .prescriptionId(item.getPrescriptionId())
            .prescriptionNo(item.getPrescriptionNo())
            .diagnosis(item.getDiagnosis())
            .status(item.getStatus())
            .totalAmount(item.getTotalAmount())
            .build()).toList();

        List<FeeSummaryResponse> feeOrders = visit == null ? List.of() : feeOrderMapper.selectList(
            new LambdaQueryWrapper<FeeOrder>().eq(FeeOrder::getVisitId, visit.getVisitId())
        ).stream().map(order -> FeeSummaryResponse.builder()
            .feeOrderId(order.getFeeOrderId())
            .orderNo(order.getOrderNo())
            .visitId(order.getVisitId())
            .businessType(order.getBusinessType())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .build()).toList();

        return PatientContextResponse.builder()
            .patientId(patient.getPatientId())
            .patientNo(patient.getPatientNo())
            .patientName(patient.getPatientName())
            .gender(patient.getGender())
            .idCard(patient.getIdCard())
            .phone(patient.getPhone())
            .registration(registration == null ? null : RegistrationSummaryResponse.builder()
                .registrationId(registration.getRegistrationId())
                .registrationNo(registration.getRegistrationNo())
                .queueNo(registration.getQueueNo())
                .feeStatus(registration.getFeeStatus())
                .status(registration.getStatus())
                .registeredAt(registration.getRegisteredAt())
                .build())
            .visit(visit == null ? null : VisitSummaryResponse.builder()
                .visitId(visit.getVisitId())
                .visitNo(visit.getVisitNo())
                .queueNo(visit.getQueueNo())
                .visitStatus(visit.getStatus())
                .startedAt(visit.getStartedAt())
                .finishedAt(visit.getFinishedAt())
                .build())
            .medicalRecord(medicalRecord == null ? null : MedicalRecordResponse.builder()
                .recordId(medicalRecord.getRecordId())
                .chiefComplaint(medicalRecord.getChiefComplaint())
                .presentIllness(medicalRecord.getPresentIllness())
                .currentTreatment(medicalRecord.getCurrentTreatment())
                .pastHistory(medicalRecord.getPastHistory())
                .allergyHistory(medicalRecord.getAllergyHistory())
                .physicalExam(medicalRecord.getPhysicalExam())
                .auxiliaryExam(medicalRecord.getAuxiliaryExam())
                .diagnosis(medicalRecord.getDiagnosis())
                .treatmentAdvice(medicalRecord.getTreatmentAdvice())
                .doctorNote(medicalRecord.getDoctorNote())
                .finalDiagnosis(medicalRecord.getFinalDiagnosis())
                .finalOpinion(medicalRecord.getFinalOpinion())
                .status(medicalRecord.getStatus())
                .build())
            .examOrders(examOrders)
            .labOrders(labOrders)
            .prescriptions(prescriptions)
            .feeOrders(feeOrders)
            .build();
    }

    public Long saveMedicalRecord(MedicalRecordSaveRequest request) {
        MedicalRecord record = request.getRecordId() != null
            ? medicalRecordMapper.selectById(request.getRecordId())
            : new MedicalRecord();
        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());

        record.setVisitId(request.getVisitId());
        record.setPatientId(request.getPatientId());
        record.setDoctorId(record.getDoctorId() != null ? record.getDoctorId() : (visit == null ? null : visit.getDoctorId()));
        record.setChiefComplaint(request.getChiefComplaint());
        record.setPresentIllness(request.getPresentIllness());
        record.setCurrentTreatment(request.getCurrentTreatment());
        record.setPastHistory(request.getPastHistory());
        record.setAllergyHistory(request.getAllergyHistory());
        record.setPhysicalExam(request.getPhysicalExam());
        record.setAuxiliaryExam(request.getAuxiliaryExam());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatmentAdvice(request.getTreatmentAdvice());
        record.setDoctorNote(request.getDoctorNote());
        record.setStatus(normalizeMedicalRecordStatus(request.getStatus()));

        if (record.getRecordId() == null) {
            medicalRecordMapper.insert(record);
        } else {
            medicalRecordMapper.updateById(record);
        }

        if (visit != null && ("待接诊".equals(visit.getStatus()) || StringUtils.hasText(record.getChiefComplaint()) || StringUtils.hasText(record.getPresentIllness()))) {
            visit.setStatus("接诊中");
            outpatientVisitMapper.updateById(visit);
        }
        return record.getRecordId();
    }

    public Long saveFinalDiagnosis(FinalDiagnosisSaveRequest request) {
        MedicalRecord record = medicalRecordMapper.selectById(request.getRecordId());
        record.setFinalDiagnosis(request.getFinalDiagnosis());
        record.setFinalOpinion(request.getFinalOpinion());
        record.setConfirmedDoctorId(record.getDoctorId());
        record.setConfirmedAt(java.time.LocalDateTime.now());
        record.setStatus("已完成");
        medicalRecordMapper.updateById(record);

        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        boolean hasPendingFee = feeOrderMapper.selectCount(
            new LambdaQueryWrapper<FeeOrder>()
                .eq(FeeOrder::getVisitId, request.getVisitId())
                .eq(FeeOrder::getStatus, "待支付")
        ) > 0;
        boolean hasPendingPrescription = prescriptionMapper.selectCount(
            new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getVisitId, request.getVisitId())
                .in(Prescription::getStatus, java.util.List.of("待缴费", "待发药", "发药中"))
        ) > 0;
        boolean hasPendingExamLab = examLabOrderMapper.selectCount(
            new LambdaQueryWrapper<ExamLabOrder>()
                .eq(ExamLabOrder::getVisitId, request.getVisitId())
                .in(ExamLabOrder::getStatus, java.util.List.of("待缴费", "待执行", "执行中"))
        ) > 0;
        visit.setStatus((hasPendingFee || hasPendingPrescription || hasPendingExamLab) ? "待处置" : "已完成");
        outpatientVisitMapper.updateById(visit);
        return record.getRecordId();
    }

    public Long skipExam(SkipRequest request) {
        return appendDecisionNote(request, "检查");
    }

    public Long skipLab(SkipRequest request) {
        return appendDecisionNote(request, "检验");
    }

    public Long startEncounter(EncounterStartRequest request) {
        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        visit.setStatus("接诊中");
        if (visit.getStartedAt() == null) {
            visit.setStartedAt(java.time.LocalDateTime.now());
        }
        outpatientVisitMapper.updateById(visit);

        MedicalRecord record = medicalRecordMapper.selectOne(
            new LambdaQueryWrapper<MedicalRecord>().eq(MedicalRecord::getVisitId, visit.getVisitId())
        );
        if (record == null) {
            record = new MedicalRecord();
            record.setVisitId(visit.getVisitId());
            record.setPatientId(visit.getPatientId());
            record.setDoctorId(visit.getDoctorId());
            record.setStatus("初诊暂存");
            medicalRecordMapper.insert(record);
        }
        return visit.getVisitId();
    }

    private Long appendDecisionNote(SkipRequest request, String type) {
        MedicalRecord record = medicalRecordMapper.selectById(request.getRecordId());
        String decisionLine = "【" + type + "决策】本次无需" + type + "。"
            + (StringUtils.hasText(request.getReason()) ? "原因：" + request.getReason() : "");
        String currentNote = StringUtils.hasText(record.getDoctorNote()) ? record.getDoctorNote() : "";
        if (!currentNote.contains(decisionLine)) {
            record.setDoctorNote(StringUtils.hasText(currentNote) ? currentNote + "\n" + decisionLine : decisionLine);
        }
        medicalRecordMapper.updateById(record);

        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        if (visit != null && "接诊中".equals(visit.getStatus())) {
            visit.setStatus("待确诊");
            outpatientVisitMapper.updateById(visit);
        }
        return record.getRecordId();
    }

    private Integer calculateAge(LocalDate birthday) {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    private String normalizeMedicalRecordStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "初诊暂存";
        }
        String normalized = status.trim();
        if (!ALLOWED_MEDICAL_RECORD_STATUSES.contains(normalized)) {
            throw new BusinessException(400, "病历状态非法，仅允许：初诊暂存、待补充、已完成、已作废");
        }
        return normalized;
    }

    private boolean matchesVisitFilter(OutpatientVisit visit, String visitStatus, String visitGroup) {
        String currentStatus = visit != null ? visit.getStatus() : null;
        if (StringUtils.hasText(visitStatus)) {
            return visitStatus.trim().equals(currentStatus);
        }
        if ("ACTIVE".equalsIgnoreCase(visitGroup)) {
            return java.util.Set.of("接诊中", "待检查检验", "检查检验中", "报告待回阅", "待确诊", "待处置").contains(currentStatus);
        }
        return true;
    }
}
