package com.neu.patient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.patient.dto.AiConsultRequest;
import com.neu.patient.entity.AiConsultation;
import com.neu.patient.entity.Department;
import com.neu.patient.mapper.AiConsultationMapper;
import com.neu.patient.mapper.DepartmentMapper;
import com.neu.patient.service.impl.AiConsultationServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiConsultationServiceImplTest {

    @Test
    void handleConsultationSavesMatchedRecommendedDepartmentId() {
        AiConsultationMapper consultationMapper = mock(AiConsultationMapper.class);
        DepartmentMapper departmentMapper = mock(DepartmentMapper.class);
        AiConsultationServiceImpl service = createService(
                consultationMapper,
                departmentMapper,
                """
                        {"summary":"考虑呼吸道感染","riskLevel":"normal","recommendedDept":"呼吸内科","recommendedDoctor":"","aiResult":"建议前往呼吸内科就诊","note":""}
                        """
        );
        Department department = new Department();
        department.setDeptId(8L);
        department.setDeptName("呼吸内科");
        when(departmentMapper.findAllActive()).thenReturn(List.of(department));

        service.handleConsultation(request("咳嗽发热两天"));

        ArgumentCaptor<AiConsultation> captor = ArgumentCaptor.forClass(AiConsultation.class);
        verify(consultationMapper).insert(captor.capture());
        assertThat(captor.getValue().getRecommendedDeptId()).isEqualTo(8L);
    }

    @Test
    void handleConsultationIgnoresDepartmentThatDoesNotExistInDatabase() {
        AiConsultationMapper consultationMapper = mock(AiConsultationMapper.class);
        DepartmentMapper departmentMapper = mock(DepartmentMapper.class);
        AiConsultationServiceImpl service = createService(
                consultationMapper,
                departmentMapper,
                """
                        {"summary":"需要线下评估","riskLevel":"normal","recommendedDept":"魔法科","recommendedDoctor":"","aiResult":"建议进一步就诊","note":""}
                        """
        );
        Department department = new Department();
        department.setDeptId(8L);
        department.setDeptName("呼吸内科");
        when(departmentMapper.findAllActive()).thenReturn(List.of(department));

        service.handleConsultation(request("咳嗽发热两天"));

        ArgumentCaptor<AiConsultation> captor = ArgumentCaptor.forClass(AiConsultation.class);
        verify(consultationMapper).insert(captor.capture());
        assertThat(captor.getValue().getRecommendedDeptId()).isNull();
    }

    private AiConsultationServiceImpl createService(
            AiConsultationMapper consultationMapper,
            DepartmentMapper departmentMapper,
            String aiResponse
    ) {
        AiConsultationServiceImpl service = new AiConsultationServiceImpl();
        AiKnowledgeService knowledgeService = (patientId, mode, content) -> List.of("科室:呼吸内科，简介:呼吸系统疾病");
        DeepSeekAiService deepSeekAiService = (systemPrompt, userPrompt) -> aiResponse;
        ReflectionTestUtils.setField(service, "aiConsultationMapper", consultationMapper);
        ReflectionTestUtils.setField(service, "departmentMapper", departmentMapper);
        ReflectionTestUtils.setField(service, "aiKnowledgeService", knowledgeService);
        ReflectionTestUtils.setField(service, "deepSeekAiService", deepSeekAiService);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
    }

    private AiConsultRequest request(String content) {
        AiConsultRequest request = new AiConsultRequest();
        request.setPatientId(1L);
        request.setMode("guide");
        request.setContent(content);
        return request;
    }
}
