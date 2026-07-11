package com.neu.patient.service;

import com.neu.patient.entity.Prescription;
import com.neu.patient.entity.PrescriptionItem;
import java.util.List;

public interface PrescriptionService {
    List<Prescription> getMyPrescriptions(Long patientId);
    Prescription getPrescriptionDetail(Long prescriptionId);
    List<PrescriptionItem> getPrescriptionItems(Long prescriptionId);
}
