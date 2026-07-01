package com.doctor.platform.ai.service;

import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.ExamLabReportContext;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.doctor.platform.modules.outpatient.entity.MedicalRecord;

import java.util.List;

public interface AiSuggestionClient {

    AiDraftBlock generateSuggestion(AiSuggestionRequest request,
                                    Patient patient,
                                    List<MedicalRecord> recentRecords,
                                    List<ExamLabOrder> recentOrders,
                                    List<ExamLabReportContext> recentReports);
}
