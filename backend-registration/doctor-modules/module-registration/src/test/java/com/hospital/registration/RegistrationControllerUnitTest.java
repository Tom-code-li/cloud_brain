package com.hospital.registration;

import com.hospital.common.core.R;
import com.hospital.registration.controller.RegistrationController;
import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.FeeChargeRequest;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.OfflineRegisterRequest;
import com.hospital.registration.domain.OnlineRegisterRequest;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.PaymentSubmitRequest;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RefundRequest;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerUnitTest {
    private static final LocalDate WORK_DATE = LocalDate.of(2026, 7, 2);

    @Mock
    private RegistrationWorkflowService service;

    private RegistrationController controller;

    @BeforeEach
    void setUp() {
        controller = new RegistrationController(service);
    }

    @Test
    void delegatesPatientDepartmentDoctorAndScheduleQueries() {
        PatientSyncRequest request = new PatientSyncRequest(
                "patient",
                "male",
                "110101199001010011",
                "13800000001",
                "none",
                "none"
        );
        PatientView patient = new PatientView(1L, "P001", "patient", "male",
                "110101199001010011", "13800000001", "none", "none");
        DepartmentView department = new DepartmentView(10L, "OUT-CARD", "Cardiology",
                "OUTPATIENT", "2F", "Cardiology clinic");
        DoctorView doctor = new DoctorView(20L, "doctor", 10L, "Cardiology",
                "Attending", "heart disease");
        DoctorScheduleView schedule = new DoctorScheduleView(30L, 20L, "doctor", 10L,
                "Cardiology", WORK_DATE, "morning", LocalTime.of(8, 0),
                LocalTime.of(12, 0), 40, 39, new BigDecimal("20.00"), "available");

        when(service.syncPatient(request)).thenReturn(patient);
        when(service.listDepartments()).thenReturn(List.of(department));
        when(service.listDoctors(10L)).thenReturn(List.of(doctor));
        when(service.listSchedules(10L, 20L, WORK_DATE)).thenReturn(List.of(schedule));

        R<PatientView> patientResponse = controller.syncPatient(request);
        R<List<DepartmentView>> departments = controller.departments();
        R<List<DoctorView>> doctors = controller.doctors(10L);
        R<List<DoctorScheduleView>> schedules = controller.schedules(10L, 20L, WORK_DATE);

        assertThat(patientResponse.getCode()).isZero();
        assertThat(patientResponse.getData()).isSameAs(patient);
        assertThat(departments.getData()).containsExactly(department);
        assertThat(doctors.getData()).containsExactly(doctor);
        assertThat(schedules.getData()).containsExactly(schedule);
        verify(service).syncPatient(request);
        verify(service).listDepartments();
        verify(service).listDoctors(10L);
        verify(service).listSchedules(10L, 20L, WORK_DATE);
    }

    @Test
    void delegatesRegistrationSubmissionPaymentAndQueueOperations() {
        OfflineRegisterRequest offlineRequest = new OfflineRegisterRequest(1L, 20L, 30L);
        OnlineRegisterRequest onlineRequest = new OnlineRegisterRequest(1L, 20L, 30L, true);
        PaymentSubmitRequest paymentRequest = new PaymentSubmitRequest(40L, "cash");
        RegistrationView registration = registrationView("REG001", "paid", "waiting");
        RegistrationView called = registrationView("REG001", "paid", "consulting");

        when(service.submitOfflineRegistration(1L, 20L, 30L)).thenReturn(registration);
        when(service.submitOnlineRegistration(1L, 20L, 30L, true)).thenReturn(registration);
        when(service.chargeRegistration(40L, "cash")).thenReturn(registration);
        when(service.callNext(20L, 30L, WORK_DATE)).thenReturn(called);
        when(service.listQueue(20L, 30L, WORK_DATE)).thenReturn(List.of(registration));
        when(service.listOnlinePending()).thenReturn(List.of(registration));
        when(service.confirmOnlineRegistration(40L)).thenReturn(registration);

        assertOkData(controller.submitOffline(offlineRequest), registration);
        assertOkData(controller.submitOnline(onlineRequest), registration);
        assertOkData(controller.charge(paymentRequest), registration);
        assertOkData(controller.call(20L, 30L, WORK_DATE), called);
        assertThat(controller.queue(20L, 30L, WORK_DATE).getData()).containsExactly(registration);
        assertThat(controller.onlinePending().getData()).containsExactly(registration);
        assertOkData(controller.confirmOnline(40L), registration);

        verify(service).submitOfflineRegistration(1L, 20L, 30L);
        verify(service).submitOnlineRegistration(1L, 20L, 30L, true);
        verify(service).chargeRegistration(40L, "cash");
        verify(service).callNext(20L, 30L, WORK_DATE);
        verify(service).listQueue(20L, 30L, WORK_DATE);
        verify(service).listOnlinePending();
        verify(service).confirmOnlineRegistration(40L);
    }

    @Test
    void delegatesFeeOrderPaymentHistoryAndRefundOperations() {
        FeeChargeRequest chargeRequest = new FeeChargeRequest(50L, "cash");
        RefundRequest refundRequest = new RefundRequest(50L, "patient cancelled");
        FeeOrderView feeOrder = feeOrder("paid");
        FeeHistoryView history = new FeeHistoryView(1L, 40L, List.of(feeOrder));
        RefundCheckView refundCheck = new RefundCheckView(50L, true, "refundable");

        when(service.chargeFeeOrder(50L, "cash")).thenReturn(feeOrder);
        when(service.listPendingFees(1L, 40L)).thenReturn(List.of(feeOrder));
        when(service.feeHistory(1L, 40L)).thenReturn(history);
        when(service.checkRefund(50L)).thenReturn(refundCheck);
        when(service.refund(50L, "patient cancelled")).thenReturn(feeOrder);

        assertOkData(controller.chargeFeeOrder(chargeRequest), feeOrder);
        assertThat(controller.pendingFees(1L, 40L).getData()).containsExactly(feeOrder);
        assertOkData(controller.feeHistory(1L, 40L), history);
        assertOkData(controller.refundCheck(50L), refundCheck);
        assertOkData(controller.refund(refundRequest), feeOrder);

        verify(service).chargeFeeOrder(50L, "cash");
        verify(service).listPendingFees(1L, 40L);
        verify(service).feeHistory(1L, 40L);
        verify(service).checkRefund(50L);
        verify(service).refund(50L, "patient cancelled");
    }

    private static RegistrationView registrationView(String registrationNo, String feeStatus, String status) {
        return new RegistrationView(40L, registrationNo, 1L, "patient", 20L, "doctor",
                "Cardiology", 30L, WORK_DATE, "morning", 1, feeStatus, status);
    }

    private static FeeOrderView feeOrder(String payStatus) {
        return new FeeOrderView(50L, 40L, 1L, "patient", 40L, "registration",
                new BigDecimal("20.00"), new BigDecimal("20.00"), BigDecimal.ZERO,
                payStatus, false, "waiting");
    }

    private static <T> void assertOkData(R<T> response, T expected) {
        assertThat(response.getCode()).isZero();
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isSameAs(expected);
    }
}
