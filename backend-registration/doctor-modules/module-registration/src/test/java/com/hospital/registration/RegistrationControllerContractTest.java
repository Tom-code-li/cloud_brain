package com.hospital.registration;

import com.hospital.common.core.R;
import com.hospital.registration.controller.RegistrationController;
import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationControllerContractTest {
    private final RegistrationWorkflowService service = new RegistrationWorkflowService();
    private final RegistrationController controller = new RegistrationController(service);

    @Test
    void exposesPatientDepartmentScheduleAndFeeQueryEndpoints() {
        R<PatientView> patientResponse = controller.syncPatient(new PatientSyncRequest(
                "赵测试",
                "男",
                "110101199001010011",
                "13600000001",
                "",
                ""
        ));

        assertThat(patientResponse.getCode()).isZero();
        assertThat(patientResponse.getData().patientName()).isEqualTo("赵测试");

        R<List<DepartmentView>> departments = controller.departments();
        assertThat(departments.getData())
                .extracting(DepartmentView::deptName)
                .contains("全科门诊");

        R<List<DoctorView>> doctors = controller.doctors(2L);
        assertThat(doctors.getData()).singleElement()
                .extracting(DoctorView::doctorName)
                .isEqualTo("王门诊");

        assertThat(controller.schedules(2L, null, LocalDate.of(2026, 6, 22)).getData())
                .singleElement()
                .satisfies(schedule -> assertThat(schedule.doctorName()).isEqualTo("王门诊"));

        RegistrationView registration = service.submitOfflineRegistration(patientResponse.getData().patientId(), 2L, 1L);
        R<List<FeeOrderView>> pendingFees = controller.pendingFees(patientResponse.getData().patientId(), registration.registrationId());

        assertThat(pendingFees.getData()).singleElement()
                .extracting(FeeOrderView::feeType)
                .isEqualTo("挂号费");
    }

    @Test
    void exposesQueueAndOnlineConfirmationEndpoints() {
        PatientView patient = service.syncPatient(new PatientSyncRequest(
                "钱线上",
                "女",
                "110101199303030033",
                "13600000003",
                "",
                ""
        ));
        RegistrationView online = service.submitOnlineRegistration(patient.patientId(), 2L, 1L, true);

        assertThat(controller.onlinePending().getData())
                .extracting(RegistrationView::registrationId)
                .containsExactly(online.registrationId());

        R<RegistrationView> confirmed = controller.confirmOnline(online.registrationId());

        assertThat(confirmed.getData().status()).isEqualTo("待接诊");
        assertThat(controller.queue(2L, 1L, null).getData())
                .extracting(RegistrationView::registrationId)
                .containsExactly(online.registrationId());
    }
}
