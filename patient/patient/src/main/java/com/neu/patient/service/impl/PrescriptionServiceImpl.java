package com.neu.patient.service.impl;

import com.neu.patient.entity.Prescription;
import com.neu.patient.entity.PrescriptionItem;
import com.neu.patient.mapper.PrescriptionMapper;
import com.neu.patient.mapper.PrescriptionItemMapper;
import com.neu.patient.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {
    @Autowired private PrescriptionMapper prescriptionMapper;
    @Autowired private PrescriptionItemMapper prescriptionItemMapper;
    @Override public List<Prescription> getMyPrescriptions(Long patientId) { return prescriptionMapper.findByPatientId(patientId); }
    @Override public Prescription getPrescriptionDetail(Long prescriptionId) { return prescriptionMapper.selectById(prescriptionId); }
    @Override public List<PrescriptionItem> getPrescriptionItems(Long prescriptionId) { return prescriptionItemMapper.findByPrescriptionId(prescriptionId); }
}
