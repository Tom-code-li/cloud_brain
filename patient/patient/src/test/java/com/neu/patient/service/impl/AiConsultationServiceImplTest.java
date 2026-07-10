package com.neu.patient.service.impl;

import com.neu.patient.dto.AiConsultRequest;
import com.neu.patient.entity.AiConsultation;
import com.neu.patient.entity.Department;
import com.neu.patient.mapper.AiConsultationMapper;
import com.neu.patient.mapper.DepartmentMapper;
import com.neu.patient.service.AiKnowledgeService;
import com.neu.patient.service.DeepSeekAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiConsultationServiceImplTest {

    @Mock
    private AiConsultationMapper aiConsultationMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private AiKnowledgeService aiKnowledgeService;
    @Mock
    private DeepSeekAiService deepSeekAiService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiConsultationServiceImpl aiConsultationService;

    private AiConsultation testConsultation;
    private Department testDept;

    @BeforeEach
    void setUp() {
        testConsultation = new AiConsultation();
        testConsultation.setConsultationId(1L);
        testConsultation.setPatientId(1L);
        testConsultation.setChiefComplaint("头痛");

        testDept = new Department();
        testDept.setDeptId(1L);
        testDept.setDeptName("神经内科");
        testDept.setDescription("诊治神经系统疾病");
    }

    @Test
    void testCreateConsultation() {
        when(aiConsultationMapper.insert(any(AiConsultation.class))).thenReturn(1);

        AiConsultation result = aiConsultationService.createConsultation(testConsultation);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals("已生成", result.getStatus());
        assertEquals("普通", result.getRiskLevel());
        verify(aiConsultationMapper).insert(any(AiConsultation.class));
    }

    @Test
    void testGetMyConsultations() {
        List<AiConsultation> consultations = Arrays.asList(testConsultation);
        when(aiConsultationMapper.findByPatientId(1L)).thenReturn(consultations);

        List<AiConsultation> result = aiConsultationService.getMyConsultations(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMyConsultationsEmpty() {
        when(aiConsultationMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<AiConsultation> result = aiConsultationService.getMyConsultations(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandleConsultation() throws Exception {
        AiConsultRequest request = new AiConsultRequest();
        request.setPatientId(1L);
        request.setMode("text");
        request.setContent("头痛三天");

        when(departmentMapper.findAllActive()).thenReturn(Arrays.asList(testDept));
        when(aiKnowledgeService.buildCandidates(1L, "text", "头痛三天"))
                .thenReturn(Arrays.asList("候选项1", "候选项2"));
        when(deepSeekAiService.chat(anyString(), anyString()))
                .thenReturn("{\"summary\":\"头痛待查\",\"riskLevel\":\"普通\",\"recommendedDept\":\"神经内科\",\"aiResult\":\"建议就诊神经内科\"}");
        when(aiConsultationMapper.insert(any(AiConsultation.class))).thenReturn(1);

        AiConsultation result = aiConsultationService.handleConsultation(request);

        assertNotNull(result);
        assertEquals("头痛三天", result.getChiefComplaint());
        verify(aiConsultationMapper).insert(any(AiConsultation.class));
    }
}
