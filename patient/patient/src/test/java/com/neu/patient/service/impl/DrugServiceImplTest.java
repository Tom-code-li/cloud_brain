package com.neu.patient.service.impl;

import com.neu.patient.entity.Drug;
import com.neu.patient.mapper.DrugMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DrugServiceImplTest {

    @Mock
    private DrugMapper drugMapper;

    @InjectMocks
    private DrugServiceImpl drugService;

    private Drug testDrug;

    @BeforeEach
    void setUp() {
        testDrug = new Drug();
        testDrug.setDrugId(1L);
        testDrug.setDrugName("阿莫西林");
        testDrug.setDrugCode("AMXL001");
        testDrug.setSpecification("0.25g*24粒");
        testDrug.setSalePrice(BigDecimal.valueOf(12.50));
        testDrug.setStatus(1);
    }

    @Test
    void testGetDrugByIdFound() {
        when(drugMapper.selectById(1L)).thenReturn(testDrug);

        Drug result = drugService.getDrugById(1L);
        assertNotNull(result);
        assertEquals("阿莫西林", result.getDrugName());
    }

    @Test
    void testGetDrugByIdNotFound() {
        when(drugMapper.selectById(999L)).thenReturn(null);

        Drug result = drugService.getDrugById(999L);
        assertNull(result);
    }

    @Test
    void testSearchDrugsByKeyword() {
        List<Drug> drugs = Arrays.asList(testDrug);
        when(drugMapper.selectList(any())).thenReturn(drugs);

        List<Drug> result = drugService.searchDrugs("阿莫西林");
        assertEquals(1, result.size());
    }

    @Test
    void testSearchDrugsEmptyResult() {
        when(drugMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Drug> result = drugService.searchDrugs("不存在药品");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchDrugsNullKeyword() {
        when(drugMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Drug> result = drugService.searchDrugs(null);
        assertTrue(result.isEmpty());
    }
}
