package com.neu.patient.service.impl;

import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.entity.Registration;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.RegistrationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class FeeServiceImplTest {

    @Mock
    private FeeOrderMapper feeOrderMapper;

    @Mock
    private RegistrationMapper registrationMapper;

    @InjectMocks
    private FeeServiceImpl feeService;

    private FeeOrder testFeeOrder;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        testFeeOrder = new FeeOrder();
        testFeeOrder.setFeeOrderId(100L);
        testFeeOrder.setPatientId(1L);
        testFeeOrder.setRegistrationId(1000L);
        testFeeOrder.setTotalAmount(BigDecimal.valueOf(20));
        testFeeOrder.setPaidAmount(BigDecimal.ZERO);
        testFeeOrder.setRefundAmount(BigDecimal.ZERO);
        testFeeOrder.setStatus(EnumValues.FEE_ORDER_WAITING_PAYMENT);

        testRegistration = new Registration();
        testRegistration.setRegistrationId(1000L);
        testRegistration.setPatientId(1L);
        testRegistration.setFeeStatus(EnumValues.FEE_WAITING_PAYMENT);
        testRegistration.setStatus(EnumValues.REGISTRATION_WAITING_PAYMENT);
    }

    // ==================== 获取费用列表 ====================

    @Test
    void testGetMyFees() {
        List<FeeOrder> fees = Arrays.asList(testFeeOrder);
        when(feeOrderMapper.findByPatientId(1L)).thenReturn(fees);

        List<FeeOrder> result = feeService.getMyFees(1L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getFeeOrderId());
    }

    @Test
    void testGetMyFeesEmpty() {
        when(feeOrderMapper.findByPatientId(999L)).thenReturn(Collections.emptyList());

        List<FeeOrder> result = feeService.getMyFees(999L);
        assertTrue(result.isEmpty());
    }

    // ==================== 获取未支付费用 ====================

    @Test
    void testGetUnpaidFees() {
        List<FeeOrder> unpaidFees = Arrays.asList(testFeeOrder);
        when(feeOrderMapper.findUnpaidByPatientId(1L)).thenReturn(unpaidFees);

        List<FeeOrder> result = feeService.getUnpaidFees(1L);
        assertEquals(1, result.size());
    }

    // ==================== 支付 ====================

    @Test
    void testPayFeeSuccess() {
        when(feeOrderMapper.selectById(100L)).thenReturn(testFeeOrder);
        when(feeOrderMapper.updateById(any(FeeOrder.class))).thenReturn(1);
        when(registrationMapper.selectById(1000L)).thenReturn(testRegistration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        boolean result = feeService.payFee(100L);
        assertTrue(result);
        assertEquals(EnumValues.FEE_ORDER_PAID, testFeeOrder.getStatus());
        assertEquals(EnumValues.FEE_PAID, testRegistration.getFeeStatus());
        assertEquals(EnumValues.REGISTRATION_WAITING_CONFIRMATION, testRegistration.getStatus());
    }

    @Test
    void testPayFeeOrderNotFound() {
        when(feeOrderMapper.selectById(999L)).thenReturn(null);

        boolean result = feeService.payFee(999L);
        assertFalse(result);
    }

    @Test
    void testPayFeeAlreadyPaid() {
        testFeeOrder.setStatus(EnumValues.FEE_ORDER_PAID);
        when(feeOrderMapper.selectById(100L)).thenReturn(testFeeOrder);

        boolean result = feeService.payFee(100L);
        assertFalse(result);
    }

    @Test
    void testPayFeeNoRegistrationLinked() {
        testFeeOrder.setRegistrationId(null);
        when(feeOrderMapper.selectById(100L)).thenReturn(testFeeOrder);
        doReturn(1).when(feeOrderMapper).updateById(Mockito.<FeeOrder>any());

        boolean result = feeService.payFee(100L);
        assertTrue(result);
        verify(registrationMapper, never()).updateById(Mockito.<Registration>any());
    }
}
