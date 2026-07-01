package com.doctor.platform;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.modules.outpatient.controller.OutpatientWorkbenchController;
import com.doctor.platform.modules.outpatient.dto.PatientListItemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FallbackDatabaseIntegrationTest {

    @Autowired
    private OutpatientWorkbenchController outpatientWorkbenchController;

    @Test
    void startsWithFallbackDatabaseAndSeedPatients() {
        ApiResponse<List<PatientListItemResponse>> response = outpatientWorkbenchController.listPatients(null, null, null, null);

        assertThat(response.getCode()).isZero();
        assertThat(response.getData()).extracting(PatientListItemResponse::getPatientNo)
            .contains("P20260622001", "P20260622002");
    }
}
