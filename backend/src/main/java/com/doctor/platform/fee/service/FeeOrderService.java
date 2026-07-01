package com.doctor.platform.fee.service;

import com.doctor.platform.fee.dto.FeeOrderItemCommand;
import com.doctor.platform.fee.dto.FeeOrderItemResponse;
import com.doctor.platform.fee.entity.FeeOrder;
import com.doctor.platform.fee.entity.FeeOrderItem;
import com.doctor.platform.fee.mapper.FeeOrderItemMapper;
import com.doctor.platform.fee.mapper.FeeOrderMapper;
import com.doctor.platform.modules.outpatient.dto.FeeItemResponse;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeeOrderService {

    private final FeeOrderMapper feeOrderMapper;
    private final FeeOrderItemMapper feeOrderItemMapper;
    private final OutpatientVisitMapper outpatientVisitMapper;

    public FeeOrderService(FeeOrderMapper feeOrderMapper,
                           FeeOrderItemMapper feeOrderItemMapper,
                           OutpatientVisitMapper outpatientVisitMapper) {
        this.feeOrderMapper = feeOrderMapper;
        this.feeOrderItemMapper = feeOrderItemMapper;
        this.outpatientVisitMapper = outpatientVisitMapper;
    }

    public Long createPendingFeeOrder(Long patientId,
                                      Long visitId,
                                      String businessType,
                                      Long businessId,
                                      List<FeeOrderItemCommand> itemCommands) {
        BigDecimal totalAmount = itemCommands.stream()
            .map(this::calculateAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        OutpatientVisit visit = visitId == null ? null : outpatientVisitMapper.selectById(visitId);

        FeeOrder feeOrder = new FeeOrder();
        feeOrder.setOrderNo("FEE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        feeOrder.setPatientId(patientId);
        feeOrder.setRegistrationId(visit == null ? null : visit.getRegistrationId());
        feeOrder.setVisitId(visitId);
        feeOrder.setBusinessType(businessType);
        feeOrder.setBusinessId(businessId);
        feeOrder.setTotalAmount(totalAmount);
        feeOrder.setPaidAmount(BigDecimal.ZERO);
        feeOrder.setRefundAmount(BigDecimal.ZERO);
        feeOrder.setStatus("待支付");
        feeOrder.setCreatedAt(LocalDateTime.now());
        feeOrderMapper.insert(feeOrder);

        for (FeeOrderItemCommand command : itemCommands) {
            FeeOrderItem item = new FeeOrderItem();
            item.setFeeOrderId(feeOrder.getFeeOrderId());
            item.setItemType(command.getItemType());
            item.setItemId(command.getItemId());
            item.setItemCode(command.getItemCode());
            item.setItemName(command.getItemName());
            item.setItemSpec(command.getItemSpec());
            item.setUnitPrice(nullToZero(command.getUnitPrice()));
            item.setQuantity(normalizeQuantity(command.getQuantity()));
            item.setAmount(calculateAmount(command));
            item.setStatus("待支付");
            item.setCreatedAt(LocalDateTime.now());
            feeOrderItemMapper.insert(item);
        }

        return feeOrder.getFeeOrderId();
    }

    public List<FeeOrderItemResponse> listFeeOrdersByVisit(Long visitId) {
        return feeOrderMapper.selectList(
            new LambdaQueryWrapper<FeeOrder>()
                .eq(FeeOrder::getVisitId, visitId)
                .orderByAsc(FeeOrder::getCreatedAt)
        ).stream().map(order -> FeeOrderItemResponse.builder()
            .feeOrderId(order.getFeeOrderId())
            .orderNo(order.getOrderNo())
            .visitId(order.getVisitId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .items(feeOrderItemMapper.selectList(
                new LambdaQueryWrapper<FeeOrderItem>()
                    .eq(FeeOrderItem::getFeeOrderId, order.getFeeOrderId())
                    .orderByAsc(FeeOrderItem::getFeeOrderItemId)
            ).stream().map(item -> FeeItemResponse.builder()
                .feeOrderItemId(item.getFeeOrderItemId())
                .itemType(item.getItemType())
                .itemId(item.getItemId())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .itemSpec(item.getItemSpec())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .amount(item.getAmount())
                .status(item.getStatus())
                .build()).toList())
            .build()).toList();
    }

    private BigDecimal calculateAmount(FeeOrderItemCommand command) {
        return nullToZero(command.getUnitPrice()).multiply(normalizeQuantity(command.getQuantity()));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : quantity;
    }
}
