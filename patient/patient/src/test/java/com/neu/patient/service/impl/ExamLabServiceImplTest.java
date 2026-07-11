package com.neu.patient.service.impl;

import com.neu.patient.entity.ExamLabOrder;
import com.neu.patient.entity.ExamLabReport;
import com.neu.patient.mapper.ExamLabOrderMapper;
import com.neu.patient.mapper.ExamLabReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamLabServiceImplTest {

    @Mock
    private ExamLabOrderMapper examLabOrderMapper;

    @Mock
    private ExamLabReportMapper examLabReportMapper;

    @InjectMocks
    private ExamLabServiceImpl examLabService;

    private ExamLabOrder testOrder;
    private ExamLabReport testReport;

    @BeforeEach
    void setUp() {
        testOrder = new ExamLabOrder();
        testOrder.setOrderId(100L);
        testOrder.setPatientId(1L);
        testOrder.setOrderType("检查");
        testOrder.setClinicalDiagnosis("血常规");

        testReport = new ExamLabReport();
        testReport.setReportId(200L);
        testReport.setPatientId(1L);
        testReport.setOrderId(100L);
        testReport.setReportType("检验");
        testReport.setStatus("已发布");
    }

    @Test
    void testGetMyOrders() {
        List<ExamLabOrder> orders = Arrays.asList(testOrder);
        when(examLabOrderMapper.findByPatientId(1L)).thenReturn(orders);

        List<ExamLabOrder> result = examLabService.getMyOrders(1L);
        assertEquals(1, result.size());
        assertEquals("检查", result.get(0).getOrderType());
    }

    @Test
    void testGetMyOrdersEmpty() {
        when(examLabOrderMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<ExamLabOrder> result = examLabService.getMyOrders(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMyReports() {
        List<ExamLabReport> reports = Arrays.asList(testReport);
        when(examLabReportMapper.findByPatientId(1L)).thenReturn(reports);

        List<ExamLabReport> result = examLabService.getMyReports(1L);
        assertEquals(1, result.size());
        assertEquals("检验", result.get(0).getReportType());
    }

    @Test
    void testGetMyReportsEmpty() {
        when(examLabReportMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<ExamLabReport> result = examLabService.getMyReports(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReportDetailFound() {
        when(examLabReportMapper.selectById(200L)).thenReturn(testReport);

        ExamLabReport result = examLabService.getReportDetail(200L);
        assertNotNull(result);
        assertEquals("已发布", result.getStatus());
    }

    @Test
    void testGetReportDetailNotFound() {
        when(examLabReportMapper.selectById(999L)).thenReturn(null);

        ExamLabReport result = examLabService.getReportDetail(999L);
        assertNull(result);
    }
}
