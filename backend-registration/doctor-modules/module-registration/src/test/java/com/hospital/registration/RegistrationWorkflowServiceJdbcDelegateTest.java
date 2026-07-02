package com.hospital.registration;

import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.repository.RegistrationJdbcRepository;
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
class RegistrationWorkflowServiceJdbcDelegateTest {
    private static final LocalDate WORK_DATE = LocalDate.of(2026, 7, 2);

    @Mock
    private RegistrationJdbcRepository repository;

    private RegistrationWorkflowService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationWorkflowService(repository);
    }

    @Test
    void delegatesCatalogAndPatientOperationsToJdbcRepository() {
        PatientSyncRequest request = new PatientSyncRequest("patient", "male",
                "110101199001010011", "13800000001", "", "");
        PatientView patient = patientView();
        DepartmentView department = new DepartmentView(2L, "GM", "General", "OUTPATIENT", "2F", "General clinic");
        DoctorView doctor = new DoctorView(2L, "doctor", 2L, "General", "Attending", "General");
        DoctorScheduleView schedule = scheduleView();

        when(repository.syncPatient(request)).thenReturn(patient);
        when(repository.findPatient(1L)).thenReturn(patient);
        when(repository.listDepartments()).thenReturn(List.of(department));
        when(repository.listDoctors(2L)).thenReturn(List.of(doctor));
        when(repository.listSchedules(2L, 2L, WORK_DATE)).thenReturn(List.of(schedule));

        assertThat(service.syncPatient(request)).isSameAs(patient);
        assertThat(service.findPatient(1L)).isSameAs(patient);
        assertThat(service.listDepartments()).containsExactly(department);
        assertThat(service.listDoctors(2L)).containsExactly(doctor);
        assertThat(service.listSchedules(2L, 2L, WORK_DATE)).containsExactly(schedule);

        verify(repository).syncPatient(request);
        verify(repository).findPatient(1L);
        verify(repository).listDepartments();
        verify(repository).listDoctors(2L);
        verify(repository).listSchedules(2L, 2L, WORK_DATE);
    }

    @Test
    void delegatesRegistrationQueueFeeAndRefundOperationsToJdbcRepository() {
        RegistrationView registration = registrationView();
        FeeOrderView feeOrder = feeOrderView();
        FeeHistoryView feeHistory = new FeeHistoryView(1L, 10L, List.of(feeOrder));
        RefundCheckView refundCheck = new RefundCheckView(50L, true, "refundable");

        when(repository.createOfflineRegistration(1L, 2L, 3L)).thenReturn(registration);
        when(repository.createOnlineRegistration(1L, 2L, 3L, true)).thenReturn(registration);
        when(repository.chargeRegistration(10L, "cash")).thenReturn(registration);
        when(repository.chargeFeeOrder(50L, "cash")).thenReturn(feeOrder);
        when(repository.confirmOnlineRegistration(10L)).thenReturn(registration);
        when(repository.listOnlinePending()).thenReturn(List.of(registration));
        when(repository.listQueue(2L, 3L, WORK_DATE)).thenReturn(List.of(registration));
        when(repository.callNext(2L, 3L, WORK_DATE)).thenReturn(registration);
        when(repository.listPendingFees(1L, 10L)).thenReturn(List.of(feeOrder));
        when(repository.feeHistory(1L, 10L)).thenReturn(feeHistory);
        when(repository.checkRefund(50L)).thenReturn(refundCheck);
        when(repository.refund(50L, "cancel")).thenReturn(feeOrder);

        assertThat(service.submitOfflineRegistration(1L, 2L, 3L)).isSameAs(registration);
        assertThat(service.submitOnlineRegistration(1L, 2L, 3L, true)).isSameAs(registration);
        assertThat(service.chargeRegistration(10L, "cash")).isSameAs(registration);
        assertThat(service.chargeFeeOrder(50L, "cash")).isSameAs(feeOrder);
        assertThat(service.confirmOnlineRegistration(10L)).isSameAs(registration);
        assertThat(service.listOnlinePending()).containsExactly(registration);
        assertThat(service.listQueue(2L, 3L, WORK_DATE)).containsExactly(registration);
        assertThat(service.callNext(2L, 3L, WORK_DATE)).isSameAs(registration);
        assertThat(service.listPendingFees(1L, 10L)).containsExactly(feeOrder);
        assertThat(service.feeHistory(1L, 10L)).isSameAs(feeHistory);
        assertThat(service.checkRefund(50L)).isSameAs(refundCheck);
        assertThat(service.refund(50L, "cancel")).isSameAs(feeOrder);

        verify(repository).createOfflineRegistration(1L, 2L, 3L);
        verify(repository).createOnlineRegistration(1L, 2L, 3L, true);
        verify(repository).chargeRegistration(10L, "cash");
        verify(repository).chargeFeeOrder(50L, "cash");
        verify(repository).confirmOnlineRegistration(10L);
        verify(repository).listOnlinePending();
        verify(repository).listQueue(2L, 3L, WORK_DATE);
        verify(repository).callNext(2L, 3L, WORK_DATE);
        verify(repository).listPendingFees(1L, 10L);
        verify(repository).feeHistory(1L, 10L);
        verify(repository).checkRefund(50L);
        verify(repository).refund(50L, "cancel");
    }

    private static PatientView patientView() {
        return new PatientView(1L, "P001", "patient", "male",
                "110101199001010011", "13800000001", "", "");
    }

    private static DoctorScheduleView scheduleView() {
        return new DoctorScheduleView(3L, 2L, "doctor", 2L, "General",
                WORK_DATE, "morning", LocalTime.of(8, 0), LocalTime.of(12, 0),
                40, 39, new BigDecimal("15.00"), "available");
    }

    private static RegistrationView registrationView() {
        return new RegistrationView(10L, "REG001", 1L, "patient", 2L, "doctor",
                "General", 3L, WORK_DATE, "morning", 1, "paid", "waiting");
    }

    private static FeeOrderView feeOrderView() {
        return new FeeOrderView(50L, 10L, 1L, "patient", 10L, "registration",
                new BigDecimal("15.00"), new BigDecimal("15.00"), BigDecimal.ZERO,
                "paid", false, "waiting");
    }
}
