package com.doctor.platform.ai.service;

import com.doctor.platform.ai.config.AiProperties;
import com.doctor.platform.ai.dto.AiDraftBlock;
import com.doctor.platform.ai.dto.AiSceneCode;
import com.doctor.platform.ai.dto.AiSuggestionRequest;
import com.doctor.platform.ai.entity.AiCallLog;
import com.doctor.platform.ai.mapper.AiCallLogMapper;
import com.doctor.platform.examlab.mapper.ExamLabReportMapper;
import com.doctor.platform.examlab.mapper.ExamLabOrderMapper;
import com.doctor.platform.examlab.mapper.ExamLabOrderItemMapper;
import com.doctor.platform.modules.outpatient.entity.Patient;
import com.doctor.platform.modules.outpatient.mapper.PatientMapper;
import com.doctor.platform.modules.outpatient.mapper.MedicalRecordMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutpatientAiServiceProviderTest {

    @Test
    void delegatesToDeepSeekClientWhenProviderIsDeepSeek() {
        PatientMapper patientMapper = mock(PatientMapper.class);
        MedicalRecordMapper medicalRecordMapper = mock(MedicalRecordMapper.class);
        ExamLabOrderMapper examLabOrderMapper = mock(ExamLabOrderMapper.class);
        ExamLabOrderItemMapper examLabOrderItemMapper = mock(ExamLabOrderItemMapper.class);
        ExamLabReportMapper examLabReportMapper = mock(ExamLabReportMapper.class);
        AiCallLogMapper aiCallLogMapper = mock(AiCallLogMapper.class);
        AiSuggestionClient aiSuggestionClient = mock(AiSuggestionClient.class);
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setProvider("deepseek");
        properties.setModelName("deepseek-v4-pro");
        properties.setApiKey("test-key");

        Patient patient = new Patient();
        patient.setPatientId(1L);
        patient.setPatientName("张晓雨");
        when(patientMapper.selectById(1L)).thenReturn(patient);
        when(medicalRecordMapper.selectList(any())).thenReturn(List.of());
        when(examLabOrderMapper.selectList(any())).thenReturn(List.of());
        when(aiSuggestionClient.generateSuggestion(any(), any(), any(), any(), any()))
            .thenReturn(AiDraftBlock.builder()
                .sceneCode(AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION)
                .diagnosisDraft("DeepSeek 诊断")
                .evidence(List.of())
                .examSuggestions(List.of())
                .drugSuggestions(List.of())
                .riskFlags(List.of())
                .build());

        OutpatientAiService service = new OutpatientAiService(
            patientMapper,
            medicalRecordMapper,
            examLabOrderMapper,
            examLabOrderItemMapper,
            examLabReportMapper,
            aiCallLogMapper,
            properties,
            aiSuggestionClient
        );
        AiSuggestionRequest request = new AiSuggestionRequest();
        request.setSceneCode(AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION);
        request.setPatientId(1L);

        AiDraftBlock block = service.generateSuggestion(request);

        assertThat(block.getDiagnosisDraft()).isEqualTo("DeepSeek 诊断");
        verify(aiSuggestionClient).generateSuggestion(any(), any(), any(), any(), any());
        verify(aiCallLogMapper).insert(any(AiCallLog.class));
    }

    @Test
    void writesDatabaseAlignedAiRoleCodeWhenSavingCallLog() {
        PatientMapper patientMapper = mock(PatientMapper.class);
        MedicalRecordMapper medicalRecordMapper = mock(MedicalRecordMapper.class);
        ExamLabOrderMapper examLabOrderMapper = mock(ExamLabOrderMapper.class);
        ExamLabOrderItemMapper examLabOrderItemMapper = mock(ExamLabOrderItemMapper.class);
        ExamLabReportMapper examLabReportMapper = mock(ExamLabReportMapper.class);
        AiCallLogMapper aiCallLogMapper = mock(AiCallLogMapper.class);
        AiSuggestionClient aiSuggestionClient = mock(AiSuggestionClient.class);
        AiProperties properties = new AiProperties();
        properties.setEnabled(false);
        properties.setProvider("simulated");

        Patient patient = new Patient();
        patient.setPatientId(1L);
        patient.setPatientName("张晓雨");
        when(patientMapper.selectById(1L)).thenReturn(patient);
        when(medicalRecordMapper.selectList(any())).thenReturn(List.of());
        when(examLabOrderMapper.selectList(any())).thenReturn(List.of());

        OutpatientAiService service = new OutpatientAiService(
            patientMapper,
            medicalRecordMapper,
            examLabOrderMapper,
            examLabOrderItemMapper,
            examLabReportMapper,
            aiCallLogMapper,
            properties,
            aiSuggestionClient
        );
        AiSuggestionRequest request = new AiSuggestionRequest();
        request.setSceneCode(AiSceneCode.OUTPATIENT_INITIAL_SUGGESTION);
        request.setPatientId(1L);

        service.generateSuggestion(request);

        var captor = forClass(AiCallLog.class);
        verify(aiCallLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getRoleCode()).isEqualTo("OUTPATIENT_AI");
        assertThat(captor.getValue().getStatus()).isEqualTo("成功");
    }
}
