package com.doctor.platform.examlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.infrastructure.exception.BusinessException;
import com.doctor.platform.examlab.dto.ExamLabOrderSaveResult;
import com.doctor.platform.examlab.dto.ExamLabOrderDetailResponse;
import com.doctor.platform.examlab.dto.ExamLabOrderItemDetailResponse;
import com.doctor.platform.examlab.dto.ExamLabOrderSaveRequest;
import com.doctor.platform.examlab.dto.ExamLabRequestForm;
import com.doctor.platform.examlab.dto.ExamLabRequestItem;
import com.doctor.platform.examlab.dto.FrontendExamLabRequest;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.examlab.entity.ExamLabOrderItem;
import com.doctor.platform.examlab.mapper.ExamLabOrderMapper;
import com.doctor.platform.examlab.mapper.ExamLabOrderItemMapper;
import com.doctor.platform.fee.dto.FeeOrderItemCommand;
import com.doctor.platform.fee.service.FeeOrderService;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExamLabOrderService {

    private final ExamLabOrderMapper examLabOrderMapper;
    private final ExamLabOrderItemMapper examLabOrderItemMapper;
    private final FeeOrderService feeOrderService;
    private final OutpatientVisitMapper outpatientVisitMapper;

    public ExamLabOrderService(ExamLabOrderMapper examLabOrderMapper,
                               ExamLabOrderItemMapper examLabOrderItemMapper,
                               FeeOrderService feeOrderService,
                               OutpatientVisitMapper outpatientVisitMapper) {
        this.examLabOrderMapper = examLabOrderMapper;
        this.examLabOrderItemMapper = examLabOrderItemMapper;
        this.feeOrderService = feeOrderService;
        this.outpatientVisitMapper = outpatientVisitMapper;
    }

    public Long saveOrder(ExamLabOrderSaveRequest request) {
        ExamLabOrder order = new ExamLabOrder();
        order.setOrderNo("EXL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        order.setVisitId(request.getVisitId());
        order.setRecordId(request.getRecordId());
        order.setPatientId(request.getPatientId());
        order.setApplyDoctorId(1L);
        order.setExecuteDeptId(1L);
        order.setOrderType(request.getOrderType());
        order.setClinicalDiagnosis(request.getClinicalDiagnosis());
        order.setPurpose(request.getPurpose());
        order.setTotalAmount(request.getTotalAmount() == null ? BigDecimal.ZERO : request.getTotalAmount());
        order.setFeeStatus("待支付");
        order.setStatus("待执行");
        order.setAppliedAt(LocalDateTime.now());
        examLabOrderMapper.insert(order);
        return order.getOrderId();
    }

    @Transactional
    public ExamLabOrderSaveResult saveFrontendRequest(FrontendExamLabRequest request, String orderType) {
        List<ExamLabRequestItem> items = "检查".equals(orderType) ? request.getExamItems() : request.getLabItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "请至少选择一个" + orderType + "项目");
        }

        ExamLabRequestForm form = request.getForm() == null ? new ExamLabRequestForm() : request.getForm();
        ExamLabOrder order = new ExamLabOrder();
        order.setOrderNo("EXL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        order.setVisitId(request.getVisitId());
        order.setRecordId(request.getRecordId());
        order.setPatientId(request.getPatientId());
        order.setApplyDoctorId(1L);
        order.setExecuteDeptId(1L);
        order.setOrderType(orderType);
        order.setClinicalDiagnosis(null);
        order.setPurpose(resolvePurpose(form));
        order.setExamSite(form.getSite());
        order.setSpecimenType(form.getSpecimen());
        order.setRemark(form.getNotes());
        order.setPriority(form.getPriority());
        order.setCollectionWay(form.getCollectionWay());
        order.setTotalAmount(calculateTotal(items));
        order.setFeeStatus("待支付");
        order.setStatus("待执行");
        order.setAppliedAt(LocalDateTime.now());
        examLabOrderMapper.insert(order);

        for (ExamLabRequestItem requestItem : items) {
            ExamLabOrderItem orderItem = new ExamLabOrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setItemId(resolveItemId(requestItem));
            orderItem.setItemName(requestItem.getName());
            orderItem.setItemType(orderType);
            orderItem.setUnitPrice(nullToZero(requestItem.getPrice()));
            orderItem.setQuantity(normalizeQuantity(requestItem.getQuantity()));
            orderItem.setAmount(calculateAmount(requestItem));
            orderItem.setStatus("待执行");
            orderItem.setCreatedAt(LocalDateTime.now());
            examLabOrderItemMapper.insert(orderItem);
        }

        Long feeOrderId = feeOrderService.createPendingFeeOrder(
            request.getPatientId(),
            request.getVisitId(),
            "EXAM_LAB_ORDER",
            order.getOrderId(),
            items.stream()
                .map(item -> FeeOrderItemCommand.builder()
                    .itemType(orderType)
                    .itemId(resolveItemId(item))
                    .itemCode(item.getCode())
                    .itemName(item.getName())
                    .itemSpec(null)
                    .unitPrice(nullToZero(item.getPrice()))
                    .quantity(normalizeQuantity(item.getQuantity()))
                    .build())
                .toList()
        );

        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        if (visit != null) {
            visit.setStatus("待检查检验");
            outpatientVisitMapper.updateById(visit);
        }

        return ExamLabOrderSaveResult.builder()
            .orderId(order.getOrderId())
            .feeOrderId(feeOrderId)
            .build();
    }

    private String resolvePurpose(ExamLabRequestForm form) {
        if (StringUtils.hasText(form.getPurpose())) {
            return form.getPurpose();
        }
        if (StringUtils.hasText(form.getNotes())) {
            return form.getNotes();
        }
        return null;
    }

    private Long resolveItemId(ExamLabRequestItem item) {
        return item.getItemId() != null ? item.getItemId() : item.getId();
    }

    private BigDecimal calculateTotal(List<ExamLabRequestItem> items) {
        return items.stream()
            .map(this::calculateAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAmount(ExamLabRequestItem item) {
        return nullToZero(item.getPrice()).multiply(normalizeQuantity(item.getQuantity()));
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : quantity;
    }

    public List<ExamLabOrderDetailResponse> listOrdersByVisit(Long visitId, String orderType) {
        LambdaQueryWrapper<ExamLabOrder> query = new LambdaQueryWrapper<ExamLabOrder>()
            .eq(ExamLabOrder::getVisitId, visitId)
            .orderByDesc(ExamLabOrder::getAppliedAt)
            .orderByDesc(ExamLabOrder::getOrderId);

        if (StringUtils.hasText(orderType)) {
            query.eq(ExamLabOrder::getOrderType, orderType.trim());
        }

        return examLabOrderMapper.selectList(query).stream()
            .map(order -> ExamLabOrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .visitId(order.getVisitId())
                .orderType(order.getOrderType())
                .purpose(order.getPurpose())
                .examSite(order.getExamSite())
                .specimenType(order.getSpecimenType())
                .remark(order.getRemark())
                .priority(order.getPriority())
                .collectionWay(order.getCollectionWay())
                .status(order.getStatus())
                .feeStatus(order.getFeeStatus())
                .totalAmount(order.getTotalAmount())
                .appliedAt(order.getAppliedAt())
                .items(examLabOrderItemMapper.selectList(
                    new LambdaQueryWrapper<ExamLabOrderItem>()
                        .eq(ExamLabOrderItem::getOrderId, order.getOrderId())
                        .orderByAsc(ExamLabOrderItem::getOrderItemId)
                ).stream().map(item -> ExamLabOrderItemDetailResponse.builder()
                    .orderItemId(item.getOrderItemId())
                    .itemId(item.getItemId())
                    .itemName(item.getItemName())
                    .itemType(item.getItemType())
                    .unitPrice(item.getUnitPrice())
                    .quantity(item.getQuantity())
                    .amount(item.getAmount())
                    .status(item.getStatus())
                    .resultSummary(item.getResultSummary())
                    .build()).toList())
                .build())
            .toList();
    }
}
