package com.neu.patient.service;

import java.util.List;

public interface AiKnowledgeService {
    List<String> buildCandidates(Long patientId, String mode, String content);
}
