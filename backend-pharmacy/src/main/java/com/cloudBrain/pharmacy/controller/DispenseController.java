package com.cloudBrain.pharmacy.controller;

import com.cloudBrain.pharmacy.common.Result;
import com.cloudBrain.pharmacy.dto.DispenseQuery;
import com.cloudBrain.pharmacy.dto.DispenseRecordDTO;
import com.cloudBrain.pharmacy.dto.DispenseStatsDTO;
import com.cloudBrain.pharmacy.dto.DrugStockDTO;
import com.cloudBrain.pharmacy.dto.MarkDispensedRequest;
import com.cloudBrain.pharmacy.dto.PurchaseRequest;
import com.cloudBrain.pharmacy.dto.PurchaseResponse;
import com.cloudBrain.pharmacy.dto.RefundRequest;
import com.cloudBrain.pharmacy.dto.RefundResponse;
import com.cloudBrain.pharmacy.dto.StockQuery;
import com.cloudBrain.pharmacy.dto.StockUpdateRequest;
import com.cloudBrain.pharmacy.service.DispenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pharmacy")
@RequiredArgsConstructor
public class DispenseController {

    private final DispenseService dispenseService;

    @GetMapping("/dispatch/queue")
    public Result<List<DispenseRecordDTO>> getQueue(@RequestParam(required = false) Long patientId,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String department,
                                                    @RequestParam(required = false) String status) {
        DispenseQuery query = new DispenseQuery();
        query.setPatientId(patientId);
        query.setKeyword(keyword);
        query.setDepartment(department);
        query.setStatus(status);
        return Result.success(dispenseService.listQueue(query));
    }

    @GetMapping("/dispense/{dispenseId}")
    public Result<DispenseRecordDTO> getDetail(@PathVariable Long dispenseId) {
        Optional<DispenseRecordDTO> detail = dispenseService.getDetail(dispenseId);
        return detail.map(Result::success).orElse(Result.notFound("未找到发药记录"));
    }

    @PostMapping("/dispense/mark")
    public Result<DispenseRecordDTO> markDispensed(@Valid @RequestBody MarkDispensedRequest request) {
        boolean ok = dispenseService.markDispensed(request.getDispenseId(), request.getPharmacist());
        if (!ok) {
            return Result.notFound("未找到发药记录");
        }
        return dispenseService.getDetail(request.getDispenseId())
                .map(record -> Result.success(record, "发药完成"))
                .orElse(Result.notFound("发药成功，但未能重新查询详情"));
    }

    @GetMapping("/stats")
    public Result<DispenseStatsDTO> getStats() {
        return Result.success(dispenseService.getStats());
    }

    @GetMapping("/refund/list")
    public Result<List<DispenseRecordDTO>> getRefundList(@RequestParam(required = false) Long patientId,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String status) {
        DispenseQuery query = new DispenseQuery();
        query.setPatientId(patientId);
        query.setKeyword(keyword);
        query.setStatus(status);
        return Result.success(dispenseService.listRefundRecords(query));
    }

    @PostMapping("/refund/submit")
    public Result<RefundResponse> submitRefund(@Valid @RequestBody RefundRequest request) {
        return Result.success(dispenseService.submitRefund(request), "退药提交成功");
    }

    @GetMapping("/stock")
    public Result<List<DrugStockDTO>> getStock(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String status) {
        StockQuery query = new StockQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        return Result.success(dispenseService.listStock(query));
    }

    @PostMapping("/stock/{drugId}")
    public Result<DrugStockDTO> updateStock(@PathVariable Long drugId,
                                            @Valid @RequestBody StockUpdateRequest request) {
        return Result.success(dispenseService.updateStock(drugId, request), "库存已更新");
    }

    @GetMapping("/purchase/list")
    public Result<List<DrugStockDTO>> getPurchaseList(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String status) {
        StockQuery query = new StockQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        return Result.success(dispenseService.listPurchaseRequests(query));
    }

    @PostMapping("/purchase/submit")
    public Result<PurchaseResponse> submitPurchase(@Valid @RequestBody PurchaseRequest request) {
        return Result.success(dispenseService.submitPurchase(request), "采购入库成功");
    }

    @GetMapping("/dispense/history")
    public Result<List<DispenseRecordDTO>> getHistory(@RequestParam(required = false) Long patientId,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String department,
                                                      @RequestParam(required = false) String status) {
        DispenseQuery query = new DispenseQuery();
        query.setPatientId(patientId);
        query.setKeyword(keyword);
        query.setDepartment(department);
        query.setStatus(status);
        return Result.success(dispenseService.listHistory(query));
    }
}
