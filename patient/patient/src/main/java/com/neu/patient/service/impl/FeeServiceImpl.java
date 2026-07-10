package com.neu.patient.service.impl;

import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.entity.Registration;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.RegistrationMapper;
import com.neu.patient.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeeServiceImpl implements FeeService {
    @Autowired private FeeOrderMapper feeOrderMapper;
    @Autowired private RegistrationMapper registrationMapper;

    @Override public List<FeeOrder> getMyFees(Long patientId) { return feeOrderMapper.findByPatientId(patientId); }
    @Override public List<FeeOrder> getUnpaidFees(Long patientId) { return feeOrderMapper.findUnpaidByPatientId(patientId); }

    @Override
    @Transactional
    public boolean payFee(Long feeOrderId) {
        FeeOrder order = feeOrderMapper.selectById(feeOrderId);
        if (order != null && EnumValues.FEE_ORDER_WAITING_PAYMENT.equals(order.getStatus())) {
            order.setStatus(EnumValues.FEE_ORDER_PAID);
            order.setPaidAmount(order.getTotalAmount());
            order.setPaidAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            feeOrderMapper.updateById(order);
            if (order.getRegistrationId() != null) {
                Registration registration = registrationMapper.selectById(order.getRegistrationId());
                if (registration != null) {
                    registration.setFeeStatus(EnumValues.FEE_PAID);
                    registration.setStatus(EnumValues.REGISTRATION_WAITING_CONFIRMATION);
                    registration.setUpdatedAt(LocalDateTime.now());
                    registrationMapper.updateById(registration);
                }
            }
            return true;
        }
        return false;
    }
}
