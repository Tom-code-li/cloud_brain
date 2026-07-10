package com.neu.patient.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.entity.Registration;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.RegistrationMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RegistrationRefundDataRepairRunner implements ApplicationRunner {
    private final RegistrationMapper registrationMapper;
    private final FeeOrderMapper feeOrderMapper;

    public RegistrationRefundDataRepairRunner(RegistrationMapper registrationMapper, FeeOrderMapper feeOrderMapper) {
        this.registrationMapper = registrationMapper;
        this.feeOrderMapper = feeOrderMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Registration> cancelledRegistrations = registrationMapper.selectList(
                new QueryWrapper<Registration>().eq("status", EnumValues.REGISTRATION_CANCELLED));

        for (Registration reg : cancelledRegistrations) {
            FeeOrder order = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                    .eq("registration_id", reg.getRegistrationId())
                    .eq("business_type", EnumValues.BUSINESS_REGISTRATION)
                    .last("LIMIT 1"));
            if (order == null) {
                continue;
            }
            if (EnumValues.FEE_ORDER_PAID.equals(order.getStatus())) {
                order.setStatus(EnumValues.FEE_ORDER_REFUNDED);
                order.setRefundAmount(order.getTotalAmount());
                order.setUpdatedAt(LocalDateTime.now());
                feeOrderMapper.updateById(order);
            }
            if (!EnumValues.FEE_REFUNDED.equals(reg.getFeeStatus()) && EnumValues.FEE_ORDER_REFUNDED.equals(order.getStatus())) {
                reg.setFeeStatus(EnumValues.FEE_REFUNDED);
                reg.setUpdatedAt(LocalDateTime.now());
                registrationMapper.updateById(reg);
            }
        }
    }
}
