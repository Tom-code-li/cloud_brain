package com.hospital.registration;

import com.hospital.common.core.BusinessException;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationDoctorWorkflowUnitTest {
    private static final String PENDING_PAYMENT = "\u5f85\u652f\u4ed8";
    private static final String PAID = "\u5df2\u652f\u4ed8";
    private static final String WAITING = "\u5f85\u63a5\u8bca";
    private static final String REFUNDED = "\u5df2\u9000\u8d39";
    private static final String REGISTRATION_FEE = "\u6302\u53f7\u8d39";
    private static final String CASH = "\u73b0\u91d1";

    @Test
    void submitOfflineRegistrationShouldCreatePendingRegistrationAndFeeOrder() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        RegistrationView registration = service.submitOfflineRegistration(101L, 2L, 1L);
        List<FeeOrderView> pendingFees = service.listPendingFees(101L, registration.registrationId());

        assertTrue(registration.registrationNo().startsWith("REG"));
        assertEquals(101L, registration.patientId());
        assertEquals(2L, registration.doctorId());
        assertEquals(1L, registration.scheduleId());
        assertEquals(PENDING_PAYMENT, registration.feeStatus());
        assertEquals(PENDING_PAYMENT, registration.status());
        assertNull(registration.queueNo());
        assertEquals(1, pendingFees.size());
        assertEquals(REGISTRATION_FEE, pendingFees.get(0).feeType());
        assertEquals(PENDING_PAYMENT, pendingFees.get(0).payStatus());
    }

    @Test
    void chargeRegistrationShouldMarkPaidAndEnterWaitingQueue() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView pending = service.submitOfflineRegistration(102L, 2L, 1L);

        RegistrationView charged = service.chargeRegistration(pending.registrationId(), CASH);
        List<RegistrationView> queue = service.listQueue(2L, 1L);

        assertEquals(PAID, charged.feeStatus());
        assertEquals(WAITING, charged.status());
        assertNotNull(charged.queueNo());
        assertEquals(1, queue.size());
        assertEquals(charged.registrationId(), queue.get(0).registrationId());
    }

    @Test
    void chargeFeeOrderShouldSynchronizeRegistrationStatus() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView pending = service.submitOfflineRegistration(103L, 2L, 1L);
        FeeOrderView feeOrder = service.listPendingFees(103L, pending.registrationId()).get(0);

        FeeOrderView chargedOrder = service.chargeFeeOrder(feeOrder.feeOrderId(), CASH);
        RegistrationView queued = service.listQueue(2L, 1L).get(0);

        assertEquals(PAID, chargedOrder.payStatus());
        assertEquals(pending.registrationId(), queued.registrationId());
        assertEquals(PAID, queued.feeStatus());
        assertEquals(WAITING, queued.status());
    }

    @Test
    void unpaidOnlineRegistrationCannotBeConfirmed() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView online = service.submitOnlineRegistration(104L, 2L, 1L, false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.confirmOnlineRegistration(online.registrationId())
        );

        assertTrue(exception.getMessage().contains("\u672a\u652f\u4ed8"));
    }

    @Test
    void paidOnlineRegistrationCanBeConfirmedIntoQueue() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView online = service.submitOnlineRegistration(105L, 2L, 1L, true);

        RegistrationView confirmed = service.confirmOnlineRegistration(online.registrationId());

        assertEquals(PAID, confirmed.feeStatus());
        assertEquals(WAITING, confirmed.status());
        assertNotNull(confirmed.queueNo());
    }

    @Test
    void registrationFeeCanBeRefundedBeforeConsultation() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView pending = service.submitOfflineRegistration(106L, 2L, 1L);
        service.chargeRegistration(pending.registrationId(), CASH);
        FeeOrderView feeOrder = service.feeHistory(106L, pending.registrationId()).orders().get(0);

        FeeOrderView refunded = service.refund(feeOrder.feeOrderId(), "patient cancelled");

        assertEquals(REFUNDED, refunded.payStatus());
        assertTrue(service.listQueue(2L, 1L).isEmpty());
    }

    @Test
    void refundedFeeOrderCannotBeChargedAgain() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        RegistrationView pending = service.submitOfflineRegistration(107L, 2L, 1L);
        FeeOrderView feeOrder = service.listPendingFees(107L, pending.registrationId()).get(0);
        service.refund(feeOrder.feeOrderId(), "patient cancelled");

        assertThrows(
                BusinessException.class,
                () -> service.chargeFeeOrder(feeOrder.feeOrderId(), CASH)
        );
    }

    @Test
    void doctorAndScheduleMismatchShouldThrowBusinessException() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();

        assertThrows(
                BusinessException.class,
                () -> service.submitOfflineRegistration(108L, 999L, 1L)
        );
    }

    @Test
    void executedExamFeeCannotBeRefunded() {
        RegistrationWorkflowService service = new RegistrationWorkflowService();
        FeeOrderView executedFee = service.demoExecutedExamFee();

        assertFalse(service.checkRefund(executedFee.feeOrderId()).refundable());
        assertThrows(
                BusinessException.class,
                () -> service.refund(executedFee.feeOrderId(), "cancel exam")
        );
    }
}
