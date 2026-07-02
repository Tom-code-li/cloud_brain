package com.hospital.registration;

import com.hospital.common.core.BusinessException;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationWorkflowServiceTest {

    private final RegistrationWorkflowService service = new RegistrationWorkflowService();

    @Test
    void feeOrderViewMatchesPublicContract() {
        assertThat(Arrays.stream(FeeOrderView.class.getRecordComponents())
                .map(recordComponent -> recordComponent.getName()))
                .containsExactly(
                        "feeOrderId",
                        "businessId",
                        "patientId",
                        "patientName",
                        "registrationId",
                        "feeType",
                        "totalAmount",
                        "paidAmount",
                        "refundAmount",
                        "payStatus",
                        "executed",
                        "businessStatus"
                );
    }

    @Test
    void offlineRegistrationCreatesPendingPaymentOrder() {
        RegistrationView registration = service.submitOfflineRegistration(1L, 2L, 1L);

        assertThat(registration.registrationNo()).startsWith("REG");
        assertThat(registration.feeStatus()).isEqualTo("待支付");
        assertThat(registration.status()).isEqualTo("待支付");
        assertThat(registration.queueNo()).isNull();
    }

    @Test
    void chargeRegistrationGeneratesQueueNumber() {
        RegistrationView pending = service.submitOfflineRegistration(1L, 2L, 1L);

        RegistrationView paid = service.chargeRegistration(pending.registrationId(), "现金");

        assertThat(paid.feeStatus()).isEqualTo("已支付");
        assertThat(paid.status()).isEqualTo("待接诊");
        assertThat(paid.queueNo()).isNotNull();
    }

    @Test
    void callNextSelectsLowestQueueNumberPaidWaitingRegistration() {
        RegistrationView firstPending = service.submitOfflineRegistration(1L, 2L, 1L);
        RegistrationView secondPending = service.submitOfflineRegistration(2L, 2L, 1L);

        RegistrationView firstPaid = service.chargeRegistration(firstPending.registrationId(), "现金");
        RegistrationView secondPaid = service.chargeRegistration(secondPending.registrationId(), "现金");

        RegistrationView called = service.callNext();

        assertThat(called.registrationId()).isEqualTo(firstPaid.registrationId());
        assertThat(called.status()).isEqualTo("接诊中");
        assertThat(called.queueNo()).isEqualTo(firstPaid.queueNo());

        RegistrationView stillWaiting = service.callNext();
        assertThat(stillWaiting.status()).isEqualTo("接诊中");
        assertThat(stillWaiting.queueNo()).isEqualTo(secondPaid.queueNo());
    }

    @Test
    void duplicateChargeAfterCallKeepsCalledStateAndDoesNotReenterQueue() {
        RegistrationView firstPending = service.submitOfflineRegistration(1L, 2L, 1L);
        RegistrationView secondPending = service.submitOfflineRegistration(2L, 2L, 1L);

        RegistrationView firstPaid = service.chargeRegistration(firstPending.registrationId(), "现金");
        RegistrationView secondPaid = service.chargeRegistration(secondPending.registrationId(), "现金");

        RegistrationView calledFirst = service.callNext();
        assertThat(calledFirst.registrationId()).isEqualTo(firstPaid.registrationId());

        RegistrationView chargedAgain = service.chargeRegistration(firstPaid.registrationId(), "现金");

        assertThat(chargedAgain.status()).isEqualTo("接诊中");
        assertThat(chargedAgain.queueNo()).isEqualTo(firstPaid.queueNo());

        RegistrationView calledSecond = service.callNext();
        assertThat(calledSecond.registrationId()).isEqualTo(secondPaid.registrationId());
        assertThat(calledSecond.status()).isEqualTo("接诊中");
        assertThat(calledSecond.queueNo()).isEqualTo(secondPaid.queueNo());
    }

    @Test
    void callNextThrowsWhenNoPaidWaitingRegistrationExists() {
        assertThatThrownBy(service::callNext)
                .isInstanceOf(BusinessException.class)
                .hasMessage("挂号记录未进入待接诊队列");
    }

    @Test
    void executedExamFeeCannotBeRefunded() {
        FeeOrderView fee = service.demoExecutedExamFee();

        assertThatThrownBy(() -> service.refund(fee.feeOrderId(), "项目已取消"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("检查/检验项目已执行，不允许退费");
    }

    @Test
    void unexecutedExamFeeCanBeRefunded() {
        FeeOrderView fee = service.demoUnexecutedExamFee();

        FeeOrderView refunded = service.refund(fee.feeOrderId(), "项目已取消");

        assertThat(refunded.payStatus()).isEqualTo("已退费");
    }

    @Test
    void refundedRegistrationFeeIsNotCallable() {
        RegistrationView pending = service.submitOfflineRegistration(1L, 2L, 1L);
        service.chargeRegistration(pending.registrationId(), "现金");

        FeeOrderView refunded = service.refund(1L, "挂号取消");

        assertThat(refunded.payStatus()).isEqualTo("已退费");
        assertThatThrownBy(service::callNext)
                .isInstanceOf(BusinessException.class)
                .hasMessage("挂号记录未进入待接诊队列");
    }
}
