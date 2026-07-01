package com.doctor.platform.outpatient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutpatientWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patientContextIncludesRegistrationVisitAndNewMedicalRecordFields() throws Exception {
        mockMvc.perform(get("/api/outpatient/patients/1/context")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.patientId").value(1))
            .andExpect(jsonPath("$.data.registration.registrationId").value(1))
            .andExpect(jsonPath("$.data.registration.registeredAt").exists())
            .andExpect(jsonPath("$.data.visit.visitId").value(1))
            .andExpect(jsonPath("$.data.visit.visitStatus").exists())
            .andExpect(jsonPath("$.data.medicalRecord.currentTreatment").exists())
            .andExpect(jsonPath("$.data.feeOrders").isArray());
    }

    @Test
    @DisplayName("queue page should only load waiting patients")
    void listPatientsCanFilterWaitingQueue() throws Exception {
        mockMvc.perform(get("/api/outpatient/patients")
                .param("visitStatus", "待接诊")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].visitStatus").value("待接诊"));
    }

    @Test
    @DisplayName("encounter page should only load active workflow patients")
    void listPatientsCanFilterActiveEncounters() throws Exception {
        mockMvc.perform(get("/api/outpatient/patients")
                .param("visitGroup", "ACTIVE")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].visitStatus").isNotEmpty());
    }

    @Test
    @DisplayName("medical record status should follow database enum constraints")
    void saveMedicalRecordRejectsUnknownStatus() throws Exception {
        mockMvc.perform(post("/api/outpatient/medical-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "patientId": 1,
                      "visitId": 1,
                      "status": "处理中"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("病历状态非法，仅允许：初诊暂存、待补充、已完成、已作废"));
    }

    @Test
    @DisplayName("starting encounter should expose initialized medical record in patient context")
    void startingEncounterMakesInitialMedicalRecordVisibleInContext() throws Exception {
        mockMvc.perform(post("/api/outpatient/start-encounter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "patientId": 2,
                      "visitId": 2
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/outpatient/patients/2/context")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.medicalRecord.status").value("初诊暂存"));
    }
}
