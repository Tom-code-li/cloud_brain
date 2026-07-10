package com.hospital.ai;

import com.hospital.ai.common.RoleAiAssistantService;
import com.hospital.ai.common.RoleAiConfigRegistry;
import com.hospital.ai.common.RoleAiRequest;
import com.hospital.ai.domain.AiCallLogView;
import com.hospital.ai.exam.ExamAiAssistant;
import com.hospital.ai.lab.LabAiAssistant;
import com.hospital.ai.outpatient.OutpatientAiAssistant;
import com.hospital.ai.pharmacy.PharmacyAiAssistant;
import com.hospital.ai.registration.RegistrationAiAssistant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleAiAssistantServiceTest {

    private final RoleAiConfigRegistry registry = RoleAiConfigRegistry.defaults();
    private final RoleAiAssistantService service = new RoleAiAssistantService(
            registry,
            List.of(
                    new RegistrationAiAssistant(registry),
                    new OutpatientAiAssistant(registry),
                    new ExamAiAssistant(registry),
                    new LabAiAssistant(registry),
                    new PharmacyAiAssistant(registry)
            )
    );

    @Test
    void eachDoctorRoleHasIndependentConfigAndApiKeyReference() {
        assertThat(registry.roleConfig("REGISTRATION").apiKeyRef()).isEqualTo("DEEPSEEK_API_KEY_REGISTRATION");
        assertThat(registry.roleConfig("OUTPATIENT").apiKeyRef()).isEqualTo("AI_KEY_OUTPATIENT");
        assertThat(registry.roleConfig("EXAM").apiKeyRef()).isEqualTo("AI_KEY_EXAM");
        assertThat(registry.roleConfig("LAB").apiKeyRef()).isEqualTo("AI_KEY_LAB");
        assertThat(registry.roleConfig("PHARMACY").apiKeyRef()).isEqualTo("AI_KEY_PHARMACY");
    }

    @Test
    void outpatientDiagnosisUsesOutpatientPromptAndRequiresDoctorConfirmation() {
        List<String> chunks = service.assist(new RoleAiRequest(
                "OUTPATIENT",
                "DIAGNOSIS",
                101L,
                2L,
                1L,
                Map.of("chiefComplaint", "咳嗽发热三天")
        ));

        String output = String.join("", chunks);
        assertThat(output).contains("门诊医生");
        assertThat(output).contains("辅助诊断");
        assertThat(output).contains("最终结论必须由对应医生确认");

        AiCallLogView log = service.logs().get(0);
        assertThat(log.roleCode()).isEqualTo("OUTPATIENT");
        assertThat(log.sceneCode()).isEqualTo("DIAGNOSIS");
        assertThat(log.businessType()).isEqualTo("OUTPATIENT.DIAGNOSIS");
        assertThat(log.changedBusinessStatus()).isFalse();
    }

    @Test
    void roleBusinessTypesMapToIndependentRoleScenesForLegacyStreamApi() {
        assertThat(service.assistLegacy("TRIAGE", 1L, Map.of("symptom", "腹痛"), 6L))
                .asString()
                .contains("挂号医生")
                .contains("分诊建议");

        assertThat(service.assistLegacy("REPORT_DRAFT", 2L, Map.of("finding", "胸片纹理增多"), 3L))
                .asString()
                .contains("检查医生")
                .contains("检查报告");

        assertThat(service.assistLegacy("LAB_RESULT_INTERPRET", 3L, Map.of("wbc", "12.5"), 4L))
                .asString()
                .contains("检验医生")
                .contains("检验结果");

        assertThat(service.assistLegacy("MEDICATION_REVIEW", 4L, Map.of("drug", "头孢"), 5L))
                .asString()
                .contains("药房医生")
                .contains("用药提醒");
    }
}
