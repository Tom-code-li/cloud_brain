package com.neu.patient.service.impl;

import com.neu.patient.entity.Prescription;
import com.neu.patient.entity.PrescriptionItem;
import com.neu.patient.mapper.PrescriptionItemMapper;
import com.neu.patient.mapper.PrescriptionMapper;
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
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Prescription testPrescription;
    private PrescriptionItem testItem;

    @BeforeEach
    void setUp() {
        testPrescription = new Prescription();
        testPrescription.setPrescriptionId(100L);
        testPrescription.setPatientId(1L);
        testPrescription.setDoctorId(10L);

        testItem = new PrescriptionItem();
        testItem.setPrescriptionItemId(1000L);
        testItem.setPrescriptionId(100L);
        testItem.setDrugName("阿莫西林");
    }

    @Test
    void testGetMyPrescriptions() {
        List<Prescription> prescriptions = Arrays.asList(testPrescription);
        when(prescriptionMapper.findByPatientId(1L)).thenReturn(prescriptions);

        List<Prescription> result = prescriptionService.getMyPrescriptions(1L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getPrescriptionId());
    }

    @Test
    void testGetMyPrescriptionsEmpty() {
        when(prescriptionMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<Prescription> result = prescriptionService.getMyPrescriptions(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPrescriptionDetailFound() {
        when(prescriptionMapper.selectById(100L)).thenReturn(testPrescription);

        Prescription result = prescriptionService.getPrescriptionDetail(100L);
        assertNotNull(result);
    }

    @Test
    void testGetPrescriptionDetailNotFound() {
        when(prescriptionMapper.selectById(999L)).thenReturn(null);

        Prescription result = prescriptionService.getPrescriptionDetail(999L);
        assertNull(result);
    }

    @Test
    void testGetPrescriptionItems() {
        List<PrescriptionItem> items = Arrays.asList(testItem);
        when(prescriptionItemMapper.findByPrescriptionId(100L)).thenReturn(items);

        List<PrescriptionItem> result = prescriptionService.getPrescriptionItems(100L);
        assertEquals(1, result.size());
        assertEquals("阿莫西林", result.get(0).getDrugName());
    }

    @Test
    void testGetPrescriptionItemsEmpty() {
        when(prescriptionItemMapper.findByPrescriptionId(999L)).thenReturn(Collections.emptyList());

        List<PrescriptionItem> result = prescriptionService.getPrescriptionItems(999L);
        assertTrue(result.isEmpty());
    }
}
