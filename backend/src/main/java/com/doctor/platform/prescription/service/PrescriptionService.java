package com.doctor.platform.prescription.service;

import com.doctor.platform.infrastructure.exception.BusinessException;
import com.doctor.platform.fee.dto.FeeOrderItemCommand;
import com.doctor.platform.fee.service.FeeOrderService;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import com.doctor.platform.prescription.dto.PrescriptionSaveResult;
import com.doctor.platform.prescription.dto.PrescriptionItemRequest;
import com.doctor.platform.prescription.dto.PrescriptionSaveRequest;
import com.doctor.platform.prescription.entity.Prescription;
import com.doctor.platform.prescription.entity.PrescriptionItem;
import com.doctor.platform.prescription.mapper.PrescriptionItemMapper;
import com.doctor.platform.prescription.mapper.PrescriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final FeeOrderService feeOrderService;
    private final OutpatientVisitMapper outpatientVisitMapper;

    public PrescriptionService(PrescriptionMapper prescriptionMapper,
                               PrescriptionItemMapper prescriptionItemMapper,
                               FeeOrderService feeOrderService,
                               OutpatientVisitMapper outpatientVisitMapper) {
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.feeOrderService = feeOrderService;
        this.outpatientVisitMapper = outpatientVisitMapper;
    }

    @Transactional
    public PrescriptionSaveResult savePrescription(PrescriptionSaveRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(400, "请至少选择一个药品");
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo("PRE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        prescription.setVisitId(request.getVisitId());
        prescription.setRecordId(request.getRecordId());
        prescription.setPatientId(request.getPatientId());
        prescription.setDoctorId(1L);
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setUsageNote(request.getUsageNote());
        prescription.setTotalAmount(resolveTotalAmount(request));
        prescription.setFeeStatus("待支付");
        prescription.setAuditStatus("审核通过");
        prescription.setStatus("待缴费");
        prescriptionMapper.insert(prescription);

        for (PrescriptionItemRequest requestItem : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getPrescriptionId());
            item.setDrugId(requestItem.getDrugId());
            item.setDrugName(requestItem.getName());
            item.setSpecification(requestItem.getSpec());
            item.setUnitPrice(nullToZero(requestItem.getPrice()));
            item.setQuantity(resolveQuantity(requestItem));
            item.setAmount(calculateAmount(requestItem));
            item.setDosage(requestItem.getDosage());
            item.setFrequency(requestItem.getFrequency());
            item.setUsageMethod(resolveUsageMethod(requestItem));
            item.setDays(requestItem.getDays());
            item.setStatus("待发药");
            item.setCreatedAt(LocalDateTime.now());
            prescriptionItemMapper.insert(item);
        }

        Long feeOrderId = feeOrderService.createPendingFeeOrder(
            request.getPatientId(),
            request.getVisitId(),
            "PRESCRIPTION",
            prescription.getPrescriptionId(),
            request.getItems().stream()
                .map(item -> FeeOrderItemCommand.builder()
                    .itemType("药品")
                    .itemId(item.getDrugId())
                    .itemCode(item.getCode())
                    .itemName(item.getName())
                    .itemSpec(item.getSpec())
                    .unitPrice(nullToZero(item.getPrice()))
                    .quantity(resolveQuantity(item))
                    .build())
                .toList()
        );

        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        if (visit != null) {
            visit.setStatus("待处置");
            outpatientVisitMapper.updateById(visit);
        }

        return PrescriptionSaveResult.builder()
            .prescriptionId(prescription.getPrescriptionId())
            .feeOrderId(feeOrderId)
            .build();
    }

    private BigDecimal resolveTotalAmount(PrescriptionSaveRequest request) {
        if (request.getTotalAmount() != null) {
            return request.getTotalAmount();
        }
        return request.getItems().stream()
            .map(this::calculateAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAmount(PrescriptionItemRequest item) {
        return nullToZero(item.getPrice()).multiply(resolveQuantity(item));
    }

    private BigDecimal resolveQuantity(PrescriptionItemRequest item) {
        if (item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            return item.getQuantity();
        }
        if (item.getCount() != null && item.getCount() > 0) {
            return BigDecimal.valueOf(item.getCount());
        }
        return BigDecimal.ONE;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String resolveUsageMethod(PrescriptionItemRequest item) {
        return StringUtils.hasText(item.getUsageMethod()) ? item.getUsageMethod() : item.getUsage();
    }
}
