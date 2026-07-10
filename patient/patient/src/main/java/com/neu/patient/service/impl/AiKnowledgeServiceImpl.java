package com.neu.patient.service.impl;

import com.neu.patient.entity.*;
import com.neu.patient.mapper.*;
import com.neu.patient.service.AiKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiKnowledgeServiceImpl implements AiKnowledgeService {
    @Autowired private DepartmentMapper departmentMapper;
    @Autowired private DoctorMapper doctorMapper;
    @Autowired private MedicalRecordMapper medicalRecordMapper;
    @Autowired private ExamLabReportMapper examLabReportMapper;
    @Autowired private PrescriptionMapper prescriptionMapper;
    @Autowired private FeeOrderMapper feeOrderMapper;
    @Autowired private RegistrationMapper registrationMapper;

    @Override
    public List<String> buildCandidates(Long patientId, String mode, String content) {
        List<String> candidates = new ArrayList<>();
        List<Department> departments = departmentMapper.findAllActive();
        List<Doctor> doctors = doctorMapper.findAllActive();
        List<MedicalRecord> records = medicalRecordMapper.findByPatientId(patientId);
        List<ExamLabReport> reports = examLabReportMapper.findByPatientId(patientId);
        List<Prescription> prescriptions = prescriptionMapper.findByPatientId(patientId);
        List<FeeOrder> feeOrders = feeOrderMapper.findByPatientId(patientId);
        List<Registration> registrations = registrationMapper.findByPatientId(patientId);

        candidates.addAll(departments.stream()
                .map(d -> "科室:" + safe(d.getDeptName()) + "，简介:" + safe(d.getDescription()))
                .collect(Collectors.toList()));

        candidates.addAll(doctors.stream()
                .limit(10)
                .map(d -> "医生:" + safe(d.getTitle()) + "，专长:" + safe(d.getSpecialty()))
                .collect(Collectors.toList()));

        candidates.addAll(records.stream()
                .limit(5)
                .map(r -> "病历:" + safe(r.getChiefComplaint()) + " | 诊断:" + safe(r.getDiagnosis()) + " | 建议:" + safe(r.getTreatmentAdvice()))
                .collect(Collectors.toList()));

        candidates.addAll(reports.stream()
                .limit(5)
                .map(r -> "检查报告:" + safe(r.getReportType()) + " | 结论:" + safe(r.getConclusion()) + " | 发现:" + safe(r.getFindings()))
                .collect(Collectors.toList()));

        candidates.addAll(prescriptions.stream()
                .limit(5)
                .map(p -> "处方:" + safe(p.getDiagnosis()) + " | 费用状态:" + safe(p.getFeeStatus()) + " | 审核状态:" + safe(p.getAuditStatus()))
                .collect(Collectors.toList()));

        candidates.addAll(feeOrders.stream()
                .limit(5)
                .map(f -> "费用单:" + safe(f.getOrderNo()) + " | 类型:" + safe(f.getBusinessType()) + " | 状态:" + safe(f.getStatus()))
                .collect(Collectors.toList()));

        candidates.addAll(registrations.stream()
                .limit(5)
                .map(r -> "挂号:" + safe(r.getRegistrationNo()) + " | 科室ID:" + r.getDeptId() + " | 医生ID:" + r.getDoctorId() + " | 状态:" + safe(r.getStatus()))
                .collect(Collectors.toList()));

        if (content != null && !content.isBlank()) {
            String lower = content.toLowerCase();
            candidates = candidates.stream()
                    .filter(item -> matchMode(mode, item) || containsAny(item, lower))
                    .collect(Collectors.toList());
        }
        return candidates.stream().distinct().limit(20).collect(Collectors.toList());
    }

    private boolean matchMode(String mode, String item) {
        if (mode == null) return true;
        return switch (mode) {
            case "guide" -> item.startsWith("科室:") || item.startsWith("医生:") || item.startsWith("挂号:");
            case "consult" -> item.startsWith("病历:") || item.startsWith("检查报告:") || item.startsWith("处方:");
            case "case" -> item.startsWith("病历:") || item.startsWith("检查报告:") || item.startsWith("处方:") || item.startsWith("费用单:");
            default -> true;
        };
    }

    private boolean containsAny(String item, String lower) {
        String s = item.toLowerCase();
        return lower.contains("咳") && s.contains("呼吸") ||
                lower.contains("痛") && (s.contains("病历") || s.contains("诊断") || s.contains("检查")) ||
                lower.contains("费") && s.contains("费用") ||
                lower.contains("检") && s.contains("检查");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
