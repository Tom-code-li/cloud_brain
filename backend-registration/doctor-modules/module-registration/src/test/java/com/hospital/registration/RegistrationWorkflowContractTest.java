package com.hospital.registration;

import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationWorkflowContractTest {
    private final RegistrationWorkflowService service = new RegistrationWorkflowService();

    @Test
    void patientSyncReusesExistingPatientByIdCard() {
        PatientView first = service.syncPatient(new PatientSyncRequest(
                "张晓雨",
                "女",
                "110101199204180021",
                "13800000001",
                "青霉素过敏",
                "无特殊既往史"
        ));

        PatientView second = service.syncPatient(new PatientSyncRequest(
                "张晓雨",
                "女",
                "110101199204180021",
                "13899990000",
                "青霉素过敏",
                "无特殊既往史"
        ));

        assertThat(second.patientId()).isEqualTo(first.patientId());
        assertThat(second.patientName()).isEqualTo("张晓雨");
        assertThat(second.phone()).isEqualTo("13899990000");
    }

    @Test
    void availableScheduleCatalogCanBeFilteredByDepartmentAndDate() {
        List<DepartmentView> departments = service.listDepartments();

        assertThat(departments)
                .extracting(DepartmentView::deptName)
                .contains("全科门诊");

        List<DoctorView> doctors = service.listDoctors(2L);
        assertThat(doctors).singleElement().satisfies(doctor -> {
            assertThat(doctor.doctorId()).isEqualTo(2L);
            assertThat(doctor.doctorName()).isEqualTo("王门诊");
            assertThat(doctor.deptName()).isEqualTo("全科门诊");
        });

        List<DoctorScheduleView> schedules = service.listSchedules(2L, null, LocalDate.of(2026, 6, 22));

        assertThat(schedules).singleElement().satisfies(schedule -> {
            assertThat(schedule.scheduleId()).isEqualTo(1L);
            assertThat(schedule.deptName()).isEqualTo("全科门诊");
            assertThat(schedule.doctorName()).isEqualTo("王门诊");
            assertThat(schedule.remainQuota()).isEqualTo(38);
            assertThat(schedule.registrationFee()).isEqualByComparingTo("15.00");
        });
    }

    @Test
    void offlineRegistrationCreatesPendingFeeAndChargeMovesPatientIntoQueue() {
        PatientView patient = service.syncPatient(new PatientSyncRequest(
                "刘建国",
                "男",
                "110101197809030039",
                "13900000002",
                "",
                "高血压病史5年"
        ));
        RegistrationView registration = service.submitOfflineRegistration(patient.patientId(), 2L, 1L);

        List<FeeOrderView> pendingFees = service.listPendingFees(patient.patientId(), registration.registrationId());

        assertThat(pendingFees).singleElement().satisfies(fee -> {
            assertThat(fee.businessId()).isEqualTo(registration.registrationId());
            assertThat(fee.feeType()).isEqualTo("挂号费");
            assertThat(fee.payStatus()).isEqualTo("待支付");
        });

        RegistrationView charged = service.chargeRegistration(registration.registrationId(), "现金");

        assertThat(charged.status()).isEqualTo("待接诊");
        assertThat(charged.queueNo()).isNotNull();
        assertThat(service.listQueue(2L, 1L))
                .extracting(RegistrationView::registrationId)
                .containsExactly(registration.registrationId());

        FeeHistoryView history = service.feeHistory(patient.patientId(), registration.registrationId());
        assertThat(history.orders()).singleElement()
                .extracting(FeeOrderView::payStatus)
                .isEqualTo("已支付");
    }

    @Test
    void paidOnlineRegistrationCanBeConfirmedIntoQueue() {
        PatientView patient = service.syncPatient(new PatientSyncRequest(
                "周线上",
                "女",
                "110101199901010099",
                "13700000003",
                "",
                ""
        ));
        RegistrationView online = service.submitOnlineRegistration(patient.patientId(), 2L, 1L, true);

        assertThat(service.listOnlinePending())
                .extracting(RegistrationView::registrationId)
                .containsExactly(online.registrationId());

        RegistrationView confirmed = service.confirmOnlineRegistration(online.registrationId());

        assertThat(confirmed.status()).isEqualTo("待接诊");
        assertThat(confirmed.feeStatus()).isEqualTo("已支付");
        assertThat(confirmed.queueNo()).isNotNull();
    }

    @Test
    void refundCheckRejectsCalledRegistrationFee() {
        RegistrationView registration = service.submitOfflineRegistration(1L, 2L, 1L);
        FeeOrderView fee = service.listPendingFees(1L, registration.registrationId()).get(0);

        service.chargeRegistration(registration.registrationId(), "现金");
        service.callNext();

        RefundCheckView check = service.checkRefund(fee.feeOrderId());

        assertThat(check.refundable()).isFalse();
        assertThat(check.reason()).isEqualTo("已接诊患者不允许退号");
    }
}
