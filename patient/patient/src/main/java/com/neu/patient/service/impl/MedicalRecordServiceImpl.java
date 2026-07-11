package com.neu.patient.service.impl;

import com.neu.patient.entity.MedicalRecord;
import com.neu.patient.mapper.MedicalRecordMapper;
import com.neu.patient.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {
    @Autowired private MedicalRecordMapper medicalRecordMapper;
    @Override public List<MedicalRecord> getMyRecords(Long patientId) { return medicalRecordMapper.findByPatientId(patientId); }
    @Override public MedicalRecord getRecordDetail(Long recordId) { return medicalRecordMapper.selectById(recordId); }
}
