package com.hospital.ai;

import com.hospital.ai.domain.AiCallLogView;
import com.hospital.ai.service.SimulatedAiService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedAiServiceTest {

    private final SimulatedAiService service = new SimulatedAiService();

    @Test
    void diagnosisPromptReturnsAssistantWarningAndSuggestion() {
        List<String> chunks = service.generateChunks("DIAGNOSIS", "咳嗽发热");

        assertThat(String.join("", chunks)).contains("仅供医生参考");
        assertThat(String.join("", chunks)).contains("建议");
    }

    @Test
    void prescriptionPromptWarnsThatDoctorMustConfirm() {
        List<String> chunks = service.generateChunks("PRESCRIPTION", "阿莫西林");

        assertThat(String.join("", chunks)).contains("最终结论必须由对应医生确认");
        assertThat(String.join("", chunks)).contains("过敏史");
    }

    @Test
    void requestAssistantLogsCallWithoutChangingBusinessStatus() {
        List<String> chunks = service.assist("REPORT_DRAFT", 42L, Map.of("result", "血常规异常"), 3L);

        assertThat(String.join("", chunks)).contains("报告");
        assertThat(service.logs()).hasSize(1);

        AiCallLogView log = service.logs().get(0);
        assertThat(log.roleCode()).isEqualTo("EXAM");
        assertThat(log.sceneCode()).isEqualTo("REPORT_DRAFT");
        assertThat(log.businessType()).isEqualTo("EXAM.REPORT_DRAFT");
        assertThat(log.businessId()).isEqualTo(42L);
        assertThat(log.doctorId()).isEqualTo(3L);
        assertThat(log.changedBusinessStatus()).isFalse();
        assertThat(log.outputText()).contains("仅供医生参考");
    }
}
