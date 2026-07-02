package com.cloudBrain.pharmacy.service;

import com.cloudBrain.pharmacy.dto.DispenseQuery;
import com.cloudBrain.pharmacy.dto.DispenseRecordDTO;
import com.cloudBrain.pharmacy.dto.DispenseStatsDTO;
import com.cloudBrain.pharmacy.dto.DrugStockDTO;
import com.cloudBrain.pharmacy.dto.PurchaseRequest;
import com.cloudBrain.pharmacy.dto.PurchaseResponse;
import com.cloudBrain.pharmacy.dto.RefundRequest;
import com.cloudBrain.pharmacy.dto.RefundResponse;
import com.cloudBrain.pharmacy.dto.StockQuery;
import com.cloudBrain.pharmacy.dto.StockUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface DispenseService {

    List<DispenseRecordDTO> listQueue(DispenseQuery query);

    Optional<DispenseRecordDTO> getDetail(Long dispenseId);

    boolean markDispensed(Long dispenseId, String pharmacist);

    DispenseStatsDTO getStats();

    List<DispenseRecordDTO> listRefundRecords(DispenseQuery query);

    RefundResponse submitRefund(RefundRequest request);

    List<DrugStockDTO> listStock(StockQuery query);

    DrugStockDTO updateStock(Long drugId, StockUpdateRequest request);

    List<DrugStockDTO> listPurchaseRequests(StockQuery query);

    PurchaseResponse submitPurchase(PurchaseRequest request);

    List<DispenseRecordDTO> listHistory(DispenseQuery query);
}
