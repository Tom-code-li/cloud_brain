package com.hospital.registration;

import com.hospital.common.core.BusinessException;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.DoctorView;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationWorkflowServiceBoundaryTest {
    private static final LocalDate SEEDED_WORK_DATE = LocalDate.of(2026, 6, 22);
    private static final LocalDate OTHER_WORK_DATE = LocalDate.of(2026, 7, 2);
    private static final String CASH = "\u73b0\u91d1";

    @Test
    void syncPatientValidatesRequiredFields() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        assertThatThrownBy(() -> service.syncPatient(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.syncPatient(new PatientSyncRequest(
                "  ",
                "\u7537",
                "110101199001010011",
                "13800000001",
                "",
                ""
        ))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.syncPatient(new PatientSyncRequest(
                "\u5f20\u4e09",
                "\u7537",
                " ",
                " ",
                "",
                ""
        ))).isInstanceOf(BusinessException.class);
    }

    @Test
    void syncPatientReusesExistingPatientByIdCardOrPhone() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        PatientView byIdCard = service.syncPatient(new PatientSyncRequest(
                "\u674e\u56db",
                "\u7537",
                "110101199001010022",
                "13800000002",
                "",
                ""
        ));
        PatientView updatedByIdCard = service.syncPatient(new PatientSyncRequest(
                "\u674e\u56db",
                "\u7537",
                "110101199001010022",
                "13900000002",
                "\u9752\u9709\u7d20\u8fc7\u654f",
                "\u65e0"
        ));
        PatientView byPhone = service.syncPatient(new PatientSyncRequest(
                "\u738b\u4e94",
                "\u5973",
                "",
                "13800000003",
                "",
                ""
        ));
        PatientView updatedByPhone = service.syncPatient(new PatientSyncRequest(
                "\u738b\u4e94",
                "\u5973",
                "",
                "13800000003",
                "\u65e0",
                "\u9ad8\u8840\u538b"
        ));

        assertThat(updatedByIdCard.patientId()).isEqualTo(byIdCard.patientId());
        assertThat(updatedByIdCard.phone()).isEqualTo("13900000002");
        assertThat(updatedByIdCard.allergyHistory()).isEqualTo("\u9752\u9709\u7d20\u8fc7\u654f");
        assertThat(updatedByPhone.patientId()).isEqualTo(byPhone.patientId());
        assertThat(updatedByPhone.pastHistory()).isEqualTo("\u9ad8\u8840\u538b");
    }

    @Test
    void findPatientReturnsExistingPatientAndRejectsUnknownId() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        PatientView patient = service.syncPatient(new PatientSyncRequest(
                "\u8d75\u516d",
                "\u7537",
                "110101199001010033",
                "",
                "",
                ""
        ));

        assertThat(service.findPatient(patient.patientId()).patientName()).isEqualTo("\u8d75\u516d");
        assertThatThrownBy(() -> service.findPatient(99999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void catalogQueriesSupportOptionalFilters() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        List<DoctorView> allDoctors = service.listDoctors(null);
        List<DoctorView> matchedDoctors = service.listDoctors(2L);
        List<DoctorView> unmatchedDoctors = service.listDoctors(999L);
        List<DoctorScheduleView> allSchedules = service.listSchedules(null, null, null);
        List<DoctorScheduleView> matchedSchedules = service.listSchedules(2L, 2L, SEEDED_WORK_DATE);
        List<DoctorScheduleView> unmatchedSchedules = service.listSchedules(2L, 2L, OTHER_WORK_DATE);

        assertThat(allDoctors).isNotEmpty();
        assertThat(matchedDoctors).extracting(DoctorView::deptId).containsOnly(2L);
        assertThat(unmatchedDoctors).isEmpty();
        assertThat(allSchedules).isNotEmpty();
        assertThat(matchedSchedules).extracting(DoctorScheduleView::workDate).containsOnly(SEEDED_WORK_DATE);
        assertThat(unmatchedSchedules).isEmpty();
    }

    @Test
    void unknownRegistrationFeeOrderAndScheduleIdsAreRejected() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        assertThatThrownBy(() -> service.submitOfflineRegistration(1L, 2L, 99999L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.chargeRegistration(99999L, CASH))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.chargeFeeOrder(99999L, CASH))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.checkRefund(99999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void confirmOnlineRegistrationRejectsOfflineRegistration() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView offline = service.submitOfflineRegistration(201L, 2L, 1L);
        service.chargeRegistration(offline.registrationId(), CASH);

        assertThatThrownBy(() -> service.confirmOnlineRegistration(offline.registrationId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void onlinePendingListExcludesConfirmedAndRefundedRegistrations() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView confirmed = service.submitOnlineRegistration(202L, 2L, 1L, true);
        RegistrationView unpaid = service.submitOnlineRegistration(203L, 2L, 1L, false);
        service.confirmOnlineRegistration(confirmed.registrationId());
        FeeOrderView unpaidFee = service.listPendingFees(203L, unpaid.registrationId()).get(0);
        service.refund(unpaidFee.feeOrderId(), "cancel");

        assertThat(service.listOnlinePending()).isEmpty();
    }

    @Test
    void queueAndCallNextRespectDateFilters() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView pending = service.submitOfflineRegistration(204L, 2L, 1L);
        service.chargeRegistration(pending.registrationId(), CASH);

        assertThat(service.listQueue(2L, 1L, OTHER_WORK_DATE)).isEmpty();
        assertThatThrownBy(() -> service.callNext(2L, 1L, OTHER_WORK_DATE))
                .isInstanceOf(BusinessException.class);
        assertThat(service.callNext(2L, 1L, SEEDED_WORK_DATE).registrationId())
                .isEqualTo(pending.registrationId());
    }

    @Test
    void refundChecksRejectCalledCanceledAndAlreadyRefundedOrders() {
        RegistrationWorkflowService calledService = new RegistrationWorkflowService();
        RegistrationView calledRegistration = calledService.submitOfflineRegistration(205L, 2L, 1L);
        calledService.chargeRegistration(calledRegistration.registrationId(), CASH);
        calledService.callNext();
        FeeOrderView calledFee = calledService.feeHistory(205L, calledRegistration.registrationId()).orders().get(0);

        RegistrationWorkflowService canceledService = new RegistrationWorkflowService();
        RegistrationView canceledRegistration = canceledService.submitOfflineRegistration(206L, 2L, 1L);
        FeeOrderView canceledFee = canceledService.listPendingFees(206L, canceledRegistration.registrationId()).get(0);
        canceledService.refund(canceledFee.feeOrderId(), "cancel");

        RegistrationWorkflowService refundedExamService = new RegistrationWorkflowService();
        FeeOrderView examFee = refundedExamService.demoUnexecutedExamFee();
        refundedExamService.refund(examFee.feeOrderId(), "cancel");

        RefundCheckView calledCheck = calledService.checkRefund(calledFee.feeOrderId());
        RefundCheckView canceledCheck = canceledService.checkRefund(canceledFee.feeOrderId());
        RefundCheckView refundedExamCheck = refundedExamService.checkRefund(examFee.feeOrderId());

        assertThat(calledCheck.refundable()).isFalse();
        assertThat(canceledCheck.refundable()).isFalse();
        assertThat(refundedExamCheck.refundable()).isFalse();
    }

    @Test
    void chargeRegistrationReturnsCanceledStateWhenFeeWasRefunded() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView registration = service.submitOfflineRegistration(207L, 2L, 1L);
        FeeOrderView fee = service.listPendingFees(207L, registration.registrationId()).get(0);
        service.refund(fee.feeOrderId(), "cancel");

        RegistrationView chargedAfterRefund = service.chargeRegistration(registration.registrationId(), CASH);

        assertThat(chargedAfterRefund.feeStatus()).isEqualTo("\u5df2\u9000\u8d39");
        assertThat(chargedAfterRefund.status()).isEqualTo("\u5df2\u53d6\u6d88");
    }

    @Test
    void scheduleQuotaCannotBeOversold() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        for (long patientId = 1; patientId <= 38; patientId++) {
            service.submitOfflineRegistration(patientId, 2L, 1L);
        }

        assertThatThrownBy(() -> service.submitOfflineRegistration(39L, 2L, 1L))
                .isInstanceOf(BusinessException.class);
    }
}
