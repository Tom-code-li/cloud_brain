package com.neu.patient.service;

import com.neu.patient.entity.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {
    List<MedicalRecord> getMyRecords(Long patientId);
    MedicalRecord getRecordDetail(Long recordId);
}
