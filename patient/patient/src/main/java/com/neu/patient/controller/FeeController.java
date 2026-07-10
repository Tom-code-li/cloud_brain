package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fee")
public class FeeController {
    @Autowired private FeeService feeService;
    @GetMapping("/my/{patientId}")
    public Result<List<FeeOrder>> myFees(@PathVariable Long patientId) {
        return Result.ok(feeService.getMyFees(patientId));
    }
    @GetMapping("/unpaid/{patientId}")
    public Result<List<FeeOrder>> unpaidFees(@PathVariable Long patientId) {
        return Result.ok(feeService.getUnpaidFees(patientId));
    }
    @PostMapping("/pay/{feeOrderId}")
    public Result<?> pay(@PathVariable Long feeOrderId) {
        return feeService.payFee(feeOrderId) ? Result.ok("支付成功") : Result.fail("支付失败");
    }
}
