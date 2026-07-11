package com.neu.patient.service.impl;

import com.neu.patient.entity.ExamLabOrder;
import com.neu.patient.entity.ExamLabReport;
import com.neu.patient.mapper.ExamLabOrderMapper;
import com.neu.patient.mapper.ExamLabReportMapper;
import com.neu.patient.service.ExamLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExamLabServiceImpl implements ExamLabService {
    @Autowired private ExamLabOrderMapper examLabOrderMapper;
    @Autowired private ExamLabReportMapper examLabReportMapper;
    @Override public List<ExamLabOrder> getMyOrders(Long patientId) { return examLabOrderMapper.findByPatientId(patientId); }
    @Override public List<ExamLabReport> getMyReports(Long patientId) { return examLabReportMapper.findByPatientId(patientId); }
    @Override public ExamLabReport getReportDetail(Long reportId) { return examLabReportMapper.selectById(reportId); }
}
