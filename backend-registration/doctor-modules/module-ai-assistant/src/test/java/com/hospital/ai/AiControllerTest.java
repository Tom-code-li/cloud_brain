package com.hospital.ai;

import com.hospital.ai.controller.AiController;
import com.hospital.ai.domain.AiCallLogView;
import com.hospital.ai.domain.AiRequest;
import com.hospital.ai.service.SimulatedAiService;
import com.hospital.common.core.R;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiControllerTest {

    @Test
    void logsReturnOkResponse() {
        AiController controller = new AiController(new SimulatedAiService(), new SyncTaskExecutor());

        R<List<AiCallLogView>> response = controller.logs();

        assertThat(response.getCode()).isZero();
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEmpty();
    }

    @Test
    void streamRecordsAiCallLog() throws Exception {
        SimulatedAiService service = new SimulatedAiService();
        AiController controller = new AiController(service, new SyncTaskExecutor());

        controller.stream(12L, new AiRequest("DIAGNOSIS", 99L, Map.of("chiefComplaint", "咳嗽")));

        assertThat(service.logs()).hasSize(1);
        assertThat(service.logs().get(0).doctorId()).isEqualTo(12L);
        assertThat(service.logs().get(0).roleCode()).isEqualTo("OUTPATIENT");
        assertThat(service.logs().get(0).sceneCode()).isEqualTo("DIAGNOSIS");
        assertThat(service.logs().get(0).businessType()).isEqualTo("OUTPATIENT.DIAGNOSIS");
        assertThat(service.logs().get(0).changedBusinessStatus()).isFalse();
    }

    @Test
    void getStreamRecordsAiCallLogForNativeEventSource() {
        SimulatedAiService service = new SimulatedAiService();
        AiController controller = new AiController(service, new SyncTaskExecutor());

        controller.streamByQuery(15L, "PRESCRIPTION", 123L, "头孢过敏");

        assertThat(service.logs()).hasSize(1);
        assertThat(service.logs().get(0).doctorId()).isEqualTo(15L);
        assertThat(service.logs().get(0).roleCode()).isEqualTo("OUTPATIENT");
        assertThat(service.logs().get(0).sceneCode()).isEqualTo("PRESCRIPTION");
        assertThat(service.logs().get(0).businessType()).isEqualTo("OUTPATIENT.PRESCRIPTION");
        assertThat(service.logs().get(0).inputText()).contains("头孢过敏");
    }

    @Test
    void roleStreamRecordsRoleAndSceneForSeparatedAiOwnership() {
        SimulatedAiService service = new SimulatedAiService();
        AiController controller = new AiController(service, new SyncTaskExecutor());

        controller.streamByRole(
                16L,
                "OUTPATIENT",
                "PRESCRIPTION",
                new AiRequest(null, 456L, Map.of("drug", "布洛芬"))
        );

        assertThat(service.logs()).hasSize(1);
        assertThat(service.logs().get(0).roleCode()).isEqualTo("OUTPATIENT");
        assertThat(service.logs().get(0).sceneCode()).isEqualTo("PRESCRIPTION");
        assertThat(service.logs().get(0).businessType()).isEqualTo("OUTPATIENT.PRESCRIPTION");
        assertThat(service.logs().get(0).doctorId()).isEqualTo(16L);
        assertThat(service.logs().get(0).changedBusinessStatus()).isFalse();
    }
}
