package com.neu.patient.service;

import com.neu.patient.entity.FeeOrder;
import java.util.List;

public interface FeeService {
    List<FeeOrder> getMyFees(Long patientId);
    List<FeeOrder> getUnpaidFees(Long patientId);
    boolean payFee(Long feeOrderId);
}
