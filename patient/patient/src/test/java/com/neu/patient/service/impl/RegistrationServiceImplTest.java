package com.neu.patient.service.impl;

import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.*;
import com.neu.patient.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private DoctorScheduleMapper doctorScheduleMapper;
    @Mock
    private RegistrationMapper registrationMapper;
    @Mock
    private FeeOrderMapper feeOrderMapper;
    @Mock
    private FeeOrderItemMapper feeOrderItemMapper;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private Department testDept;
    private Doctor testDoctor;
    private DoctorSchedule testSchedule;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        testDept = new Department();
        testDept.setDeptId(1L);
        testDept.setDeptName("内科");

        testDoctor = new Doctor();
        testDoctor.setDoctorId(10L);
        testDoctor.setDeptId(1L);

        testSchedule = new DoctorSchedule();
        testSchedule.setScheduleId(100L);
        testSchedule.setDoctorId(10L);
        testSchedule.setDeptId(1L);
        testSchedule.setWorkDate(LocalDate.now());
        testSchedule.setTimePeriod("上午");
        testSchedule.setStartTime(LocalTime.of(8, 0));
        testSchedule.setEndTime(LocalTime.of(12, 0));
        testSchedule.setTotalQuota(30);
        testSchedule.setRemainQuota(5);
        testSchedule.setRegistrationFee(BigDecimal.valueOf(20));
        testSchedule.setStatus(EnumValues.SCHEDULE_AVAILABLE);

        testRegistration = new Registration();
        testRegistration.setRegistrationId(1000L);
        testRegistration.setPatientId(1L);
        testRegistration.setDeptId(1L);
        testRegistration.setDoctorId(10L);
        testRegistration.setScheduleId(100L);
        testRegistration.setRegistrationFee(BigDecimal.valueOf(20));
    }

    // ==================== 获取科室列表 ====================

    @Test
    void testGetAllDepartments() {
        List<Department> departments = Arrays.asList(testDept);
        when(departmentMapper.findAllActive()).thenReturn(departments);

        List<Department> result = registrationService.getAllDepartments();
        assertEquals(1, result.size());
        assertEquals("内科", result.get(0).getDeptName());
    }

    @Test
    void testGetAllDepartmentsEmpty() {
        when(departmentMapper.findAllActive()).thenReturn(Collections.emptyList());

        List<Department> result = registrationService.getAllDepartments();
        assertTrue(result.isEmpty());
    }

    // ==================== 获取科室医生 ====================

    @Test
    void testGetDoctorsByDept() {
        List<Doctor> doctors = Arrays.asList(testDoctor);
        when(doctorMapper.findByDeptId(1L)).thenReturn(doctors);

        List<Doctor> result = registrationService.getDoctorsByDept(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testGetDoctorsByDeptEmpty() {
        when(doctorMapper.findByDeptId(999L)).thenReturn(Collections.emptyList());

        List<Doctor> result = registrationService.getDoctorsByDept(999L);
        assertTrue(result.isEmpty());
    }

    // ==================== 获取医生排班 ====================

    @Test
    void testGetDoctorSchedules() {
        List<DoctorSchedule> schedules = Arrays.asList(testSchedule);
        when(doctorScheduleMapper.findFutureByDoctorId(10L)).thenReturn(schedules);

        List<DoctorSchedule> result = registrationService.getDoctorSchedules(10L);
        assertEquals(1, result.size());
    }

    // ==================== 预约挂号 ====================

    @Test
    void testRegisterAppointmentSuccess() {
        when(doctorScheduleMapper.selectById(100L)).thenReturn(testSchedule);
        when(registrationMapper.insert(any(Registration.class))).thenReturn(1);
        when(feeOrderMapper.insert(any(FeeOrder.class))).thenReturn(1);
        when(feeOrderItemMapper.insert(any(FeeOrderItem.class))).thenReturn(1);
        when(doctorScheduleMapper.updateById(any(DoctorSchedule.class))).thenReturn(1);

        Registration result = registrationService.registerAppointment(testRegistration);

        assertNotNull(result);
        assertNotNull(result.getRegistrationNo());
        assertEquals(EnumValues.FEE_WAITING_PAYMENT, result.getFeeStatus());
        assertEquals(EnumValues.REGISTRATION_WAITING_PAYMENT, result.getStatus());
        // 验证余号减1
        assertEquals(4, testSchedule.getRemainQuota());
    }

    @Test
    void testRegisterAppointmentScheduleNotFound() {
        when(doctorScheduleMapper.selectById(999L)).thenReturn(null);
        testRegistration.setScheduleId(999L);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerAppointment(testRegistration));
    }

    @Test
    void testRegisterAppointmentScheduleStopped() {
        testSchedule.setStatus(EnumValues.SCHEDULE_STOPPED);
        when(doctorScheduleMapper.selectById(100L)).thenReturn(testSchedule);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerAppointment(testRegistration));
    }

    @Test
    void testRegisterAppointmentNoRemainQuota() {
        testSchedule.setRemainQuota(0);
        when(doctorScheduleMapper.selectById(100L)).thenReturn(testSchedule);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerAppointment(testRegistration));
    }

    @Test
    void testRegisterAppointmentQuotaBecomesZeroSetsFull() {
        testSchedule.setRemainQuota(1);
        when(doctorScheduleMapper.selectById(100L)).thenReturn(testSchedule);
        when(registrationMapper.insert(any(Registration.class))).thenReturn(1);
        when(feeOrderMapper.insert(any(FeeOrder.class))).thenReturn(1);
        when(feeOrderItemMapper.insert(any(FeeOrderItem.class))).thenReturn(1);
        when(doctorScheduleMapper.updateById(any(DoctorSchedule.class))).thenReturn(1);

        registrationService.registerAppointment(testRegistration);

        assertEquals(EnumValues.SCHEDULE_FULL, testSchedule.getStatus());
    }

    // ==================== 获取我的挂号 ====================

    @Test
    void testGetMyRegistrations() {
        List<Registration> registrations = Arrays.asList(testRegistration);
        when(registrationMapper.findByPatientId(1L)).thenReturn(registrations);

        List<Registration> result = registrationService.getMyRegistrations(1L);
        assertEquals(1, result.size());
    }

    // ==================== 取消挂号 ====================

    @Test
    void testCancelRegistrationSuccess() {
        testRegistration.setStatus(EnumValues.REGISTRATION_WAITING_PAYMENT);
        testRegistration.setFeeStatus(EnumValues.FEE_WAITING_PAYMENT);
        when(registrationMapper.selectById(1000L)).thenReturn(testRegistration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);
        when(doctorScheduleMapper.selectById(100L)).thenReturn(testSchedule);
        when(doctorScheduleMapper.updateById(any(DoctorSchedule.class))).thenReturn(1);

        boolean result = registrationService.cancelRegistration(1000L);
        assertTrue(result);
        assertEquals(EnumValues.REGISTRATION_CANCELLED, testRegistration.getStatus());
        assertEquals(6, testSchedule.getRemainQuota()); // 余号加回
    }

    @Test
    void testCancelRegistrationAlreadyCancelled() {
        testRegistration.setStatus(EnumValues.REGISTRATION_CANCELLED);
        testRegistration.setRegistrationId(2000L);
        when(registrationMapper.selectById(2000L)).thenReturn(testRegistration);

        boolean result = registrationService.cancelRegistration(2000L);
        assertFalse(result);
    }

    @Test
    void testCancelRegistrationNotFound() {
        when(registrationMapper.selectById(9999L)).thenReturn(null);

        boolean result = registrationService.cancelRegistration(9999L);
        assertFalse(result);
    }
}
