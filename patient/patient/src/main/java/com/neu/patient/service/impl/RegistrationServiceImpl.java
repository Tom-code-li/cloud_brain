package com.neu.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.*;
import com.neu.patient.mapper.*;
import com.neu.patient.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired private DepartmentMapper departmentMapper;
    @Autowired private DoctorMapper doctorMapper;
    @Autowired private DoctorScheduleMapper doctorScheduleMapper;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private FeeOrderMapper feeOrderMapper;
    @Autowired private FeeOrderItemMapper feeOrderItemMapper;

    @Override public List<Department> getAllDepartments() { return departmentMapper.findAllActive(); }
    @Override public List<Doctor> getDoctorsByDept(Long deptId) { return doctorMapper.findByDeptId(deptId); }
    @Override public List<DoctorSchedule> getDoctorSchedules(Long doctorId) { return doctorScheduleMapper.findFutureByDoctorId(doctorId); }

    @Override
    @Transactional
    public Registration registerAppointment(Registration reg) {
        DoctorSchedule schedule = doctorScheduleMapper.selectById(reg.getScheduleId());
        if (schedule == null) {
            throw new IllegalStateException("该预约时间段不可预约");
        }
        if (EnumValues.SCHEDULE_STOPPED.equals(schedule.getStatus())) {
            throw new IllegalStateException("该时间段已停用");
        }
        if (schedule.getRemainQuota() == null || schedule.getRemainQuota() <= 0
                || !EnumValues.SCHEDULE_AVAILABLE.equals(schedule.getStatus())) {
            throw new IllegalStateException("该时间段可预约人数为零");
        }
        reg.setRegistrationNo("RG" + System.currentTimeMillis());
        reg.setFeeStatus(EnumValues.FEE_WAITING_PAYMENT);
        reg.setStatus(EnumValues.REGISTRATION_WAITING_PAYMENT);
        reg.setRegisteredAt(LocalDateTime.now());
        reg.setCreatedAt(LocalDateTime.now());
        reg.setUpdatedAt(LocalDateTime.now());
        registrationMapper.insert(reg);
        createRegistrationFeeOrder(reg);
        schedule.setRemainQuota(schedule.getRemainQuota() - 1);
        if (schedule.getRemainQuota() == 0) {
            schedule.setStatus(EnumValues.SCHEDULE_FULL);
        }
        doctorScheduleMapper.updateById(schedule);
        return reg;
    }

    private void createRegistrationFeeOrder(Registration reg) {
        FeeOrder order = new FeeOrder();
        order.setOrderNo("FO" + System.currentTimeMillis() + reg.getRegistrationId());
        order.setPatientId(reg.getPatientId());
        order.setRegistrationId(reg.getRegistrationId());
        order.setBusinessType(EnumValues.BUSINESS_REGISTRATION);
        order.setBusinessId(reg.getRegistrationId());
        order.setTotalAmount(reg.getRegistrationFee() == null ? BigDecimal.ZERO : reg.getRegistrationFee());
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRefundAmount(BigDecimal.ZERO);
        order.setStatus(EnumValues.FEE_ORDER_WAITING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        feeOrderMapper.insert(order);
        createRegistrationFeeOrderItem(order);
    }

    private void createRegistrationFeeOrderItem(FeeOrder order) {
        FeeOrderItem item = new FeeOrderItem();
        item.setFeeOrderId(order.getFeeOrderId());
        item.setItemType(EnumValues.ITEM_REGISTRATION);
        item.setItemName("挂号费");
        item.setUnitPrice(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
        item.setQuantity(BigDecimal.ONE);
        item.setAmount(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
        item.setStatus(EnumValues.FEE_ORDER_WAITING_PAYMENT);
        item.setCreatedAt(LocalDateTime.now());
        feeOrderItemMapper.insert(item);
    }

    @Override public List<Registration> getMyRegistrations(Long patientId) { return registrationMapper.findByPatientId(patientId); }

    @Override
    @Transactional
    public boolean cancelRegistration(Long registrationId) {
        Registration reg = registrationMapper.selectById(registrationId);
        if (reg != null && (EnumValues.REGISTRATION_WAITING_PAYMENT.equals(reg.getStatus())
                || EnumValues.REGISTRATION_WAITING_CONFIRMATION.equals(reg.getStatus()))) {
            boolean refunded = handleRefundForCancelledRegistration(reg);
            if (refunded) {
                reg.setFeeStatus(EnumValues.FEE_REFUNDED);
            } else if (!EnumValues.FEE_WAITING_PAYMENT.equals(reg.getFeeStatus())) {
                reg.setFeeStatus(EnumValues.FEE_WAITING_PAYMENT);
            }
            reg.setStatus(EnumValues.REGISTRATION_CANCELLED);
            reg.setUpdatedAt(LocalDateTime.now());
            registrationMapper.updateById(reg);
            if (reg.getScheduleId() != null) {
                DoctorSchedule schedule = doctorScheduleMapper.selectById(reg.getScheduleId());
                if (schedule != null) {
                    schedule.setRemainQuota(schedule.getRemainQuota() + 1);
                    doctorScheduleMapper.updateById(schedule);
                }
            }
            return true;
        }
        return false;
    }

    private boolean handleRefundForCancelledRegistration(Registration reg) {
        FeeOrder order = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                .eq("registration_id", reg.getRegistrationId())
                .eq("business_type", EnumValues.BUSINESS_REGISTRATION)
                .last("LIMIT 1"));
        if (order == null) {
            return false;
        }
        if (EnumValues.FEE_ORDER_PAID.equals(order.getStatus())) {
            order.setStatus(EnumValues.FEE_ORDER_REFUNDED);
            order.setRefundAmount(order.getTotalAmount());
            order.setUpdatedAt(LocalDateTime.now());
            feeOrderMapper.updateById(order);
            return true;
        } else if (EnumValues.FEE_ORDER_WAITING_PAYMENT.equals(order.getStatus())) {
            reg.setFeeStatus(EnumValues.FEE_WAITING_PAYMENT);
        }
        return false;
    }
}
