package com.neu.patient.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.DoctorSchedule;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.entity.Patient;
import com.neu.patient.entity.Registration;
import com.neu.patient.mapper.DoctorScheduleMapper;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.PatientMapper;
import com.neu.patient.mapper.RegistrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RegistrationPaymentFlowTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private FeeService feeService;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DoctorScheduleMapper doctorScheduleMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private FeeOrderMapper feeOrderMapper;

    @Test
    void registerAppointmentCreatesUnpaidFeeOrder() {
        Registration registration = createRegistration();

        Registration saved = registrationService.registerAppointment(registration);

        FeeOrder order = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                .eq("registration_id", saved.getRegistrationId()));
        assertThat(order).isNotNull();
        assertThat(order.getPatientId()).isEqualTo(saved.getPatientId());
        assertThat(order.getBusinessType()).isEqualTo(EnumValues.BUSINESS_REGISTRATION);
        assertThat(order.getBusinessId()).isEqualTo(saved.getRegistrationId());
        assertThat(order.getTotalAmount()).isEqualByComparingTo(saved.getRegistrationFee());
        assertThat(order.getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(order.getStatus()).isEqualTo(EnumValues.FEE_ORDER_WAITING_PAYMENT);
        assertThat(saved.getStatus()).isEqualTo(EnumValues.REGISTRATION_WAITING_PAYMENT);
    }

    @Test
    void payFeeMarksOrderPaidAndRegistrationPaid() {
        Registration saved = registrationService.registerAppointment(createRegistration());
        FeeOrder order = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                .eq("registration_id", saved.getRegistrationId()));

        boolean paid = feeService.payFee(order.getFeeOrderId());

        FeeOrder paidOrder = feeOrderMapper.selectById(order.getFeeOrderId());
        Registration paidRegistration = registrationMapper.selectById(saved.getRegistrationId());
        assertThat(paid).isTrue();
        assertThat(paidOrder.getStatus()).isEqualTo(EnumValues.FEE_ORDER_PAID);
        assertThat(paidOrder.getPaidAmount()).isEqualByComparingTo(paidOrder.getTotalAmount());
        assertThat(paidOrder.getPaidAt()).isNotNull();
        assertThat(paidRegistration.getFeeStatus()).isEqualTo(EnumValues.FEE_PAID);
    }

    @Test
    void cancelPaidRegistrationMarksRefunded() {
        Registration saved = registrationService.registerAppointment(createRegistration());
        FeeOrder order = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                .eq("registration_id", saved.getRegistrationId()));
        feeService.payFee(order.getFeeOrderId());

        boolean cancelled = registrationService.cancelRegistration(saved.getRegistrationId());

        Registration cancelledRegistration = registrationMapper.selectById(saved.getRegistrationId());
        FeeOrder refundedOrder = feeOrderMapper.selectById(order.getFeeOrderId());
        assertThat(cancelled).isTrue();
        assertThat(cancelledRegistration.getStatus()).isEqualTo(EnumValues.REGISTRATION_CANCELLED);
        assertThat(cancelledRegistration.getFeeStatus()).isEqualTo(EnumValues.FEE_REFUNDED);
        assertThat(refundedOrder.getStatus()).isEqualTo(EnumValues.FEE_ORDER_REFUNDED);
        assertThat(refundedOrder.getRefundAmount()).isEqualByComparingTo(refundedOrder.getTotalAmount());
    }

    private Registration createRegistration() {
        Patient patient = patientMapper.selectOne(new QueryWrapper<Patient>().last("LIMIT 1"));
        DoctorSchedule schedule = doctorScheduleMapper.selectOne(new QueryWrapper<DoctorSchedule>()
                .eq("status", EnumValues.SCHEDULE_AVAILABLE)
                .gt("remain_quota", 0)
                .last("LIMIT 1"));
        if (schedule == null) {
            schedule = doctorScheduleMapper.selectOne(new QueryWrapper<DoctorSchedule>()
                    .eq("status", "active")
                    .gt("remain_quota", 0)
                    .last("LIMIT 1"));
            if (schedule != null) {
                schedule.setStatus(EnumValues.SCHEDULE_AVAILABLE);
                schedule.setTimePeriod(toStandardTimePeriod(schedule.getTimePeriod()));
                doctorScheduleMapper.updateById(schedule);
            }
        }
        assertThat(patient).isNotNull();
        assertThat(schedule).isNotNull();

        Registration registration = new Registration();
        registration.setPatientId(patient.getPatientId());
        registration.setDeptId(schedule.getDeptId());
        registration.setDoctorId(schedule.getDoctorId());
        registration.setScheduleId(schedule.getScheduleId());
        registration.setSource(EnumValues.SOURCE_ONLINE);
        registration.setRegistrationFee(schedule.getRegistrationFee());
        return registration;
    }

    private String toStandardTimePeriod(String timePeriod) {
        if ("morning".equals(timePeriod)) {
            return EnumValues.SCHEDULE_MORNING;
        }
        if ("afternoon".equals(timePeriod)) {
            return EnumValues.SCHEDULE_AFTERNOON;
        }
        if ("night".equals(timePeriod)) {
            return EnumValues.SCHEDULE_NIGHT;
        }
        return timePeriod;
    }
}
