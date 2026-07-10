package com.neu.patient.service.impl;

import com.neu.patient.entity.MedicalRecord;
import com.neu.patient.mapper.MedicalRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private MedicalRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new MedicalRecord();
        testRecord.setRecordId(1L);
        testRecord.setPatientId(1L);
        testRecord.setDoctorId(10L);
        testRecord.setChiefComplaint("头痛");
        testRecord.setDiagnosis("感冒");
    }

    @Test
    void testGetMyRecords() {
        List<MedicalRecord> records = Arrays.asList(testRecord);
        when(medicalRecordMapper.findByPatientId(1L)).thenReturn(records);

        List<MedicalRecord> result = medicalRecordService.getMyRecords(1L);
        assertEquals(1, result.size());
        assertEquals("头痛", result.get(0).getChiefComplaint());
    }

    @Test
    void testGetMyRecordsEmpty() {
        when(medicalRecordMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<MedicalRecord> result = medicalRecordService.getMyRecords(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecordDetailFound() {
        when(medicalRecordMapper.selectById(1L)).thenReturn(testRecord);

        MedicalRecord result = medicalRecordService.getRecordDetail(1L);
        assertNotNull(result);
        assertEquals("感冒", result.getDiagnosis());
    }

    @Test
    void testGetRecordDetailNotFound() {
        when(medicalRecordMapper.selectById(999L)).thenReturn(null);

        MedicalRecord result = medicalRecordService.getRecordDetail(999L);
        assertNull(result);
    }
}
