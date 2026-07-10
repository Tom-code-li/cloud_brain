package com.neu.patient.service;

import com.neu.patient.entity.AiConsultation;
import java.util.List;

public interface AiConsultationService {
    AiConsultation createConsultation(AiConsultation consultation);
    List<AiConsultation> getMyConsultations(Long patientId);
}
