package com.neu.patient.service;

import com.neu.patient.entity.ExamLabOrder;
import com.neu.patient.entity.ExamLabReport;
import java.util.List;

public interface ExamLabService {
    List<ExamLabOrder> getMyOrders(Long patientId);
    List<ExamLabReport> getMyReports(Long patientId);
    ExamLabReport getReportDetail(Long reportId);
}
