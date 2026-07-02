package com.cloudBrain.pharmacy.assembler;

import com.cloudBrain.pharmacy.dto.DispenseItemDTO;
import com.cloudBrain.pharmacy.dto.DispenseRecordDTO;
import com.cloudBrain.pharmacy.entity.DispenseItem;
import com.cloudBrain.pharmacy.entity.DispenseRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 发药领域装配器
 * 单一职责：只负责 Entity 与 DTO 之间的字段映射和组装
 * 严格隔离：数据库 Entity 永不出 Service 层，DTO 是外部契约
 */
@Component
public class DispenseAssembler {

    public DispenseRecordDTO toDTO(DispenseRecord record, List<DispenseItemDTO> items) {
        if (record == null) {
            return null;
        }
        DispenseRecordDTO dto = new DispenseRecordDTO();
        dto.setDispenseId(record.getDispenseId());
        dto.setPrescriptionNo(record.getPrescriptionNo());
        dto.setPatientId(record.getPatientId());
        dto.setPatientName(record.getPatientName());
        dto.setGender(record.getGender());
        dto.setAge(record.getAge());
        dto.setDepartment(record.getDepartment());
        dto.setDoctorName(record.getDoctorName());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTotalAmount(record.getTotalAmount());
        dto.setPayStatus(record.getPayStatus());
        dto.setStatus(record.getStatus());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setItems(items);
        return dto;
    }

    public DispenseItemDTO toItemDTO(DispenseItem item, Map<String, Integer> stockMap) {
        if (item == null) {
            return null;
        }
        DispenseItemDTO dto = new DispenseItemDTO();
        dto.setDrugId(parseDrugId(item.getDrugId()));
        dto.setDrugName(item.getDrugName());
        dto.setSpecification(item.getSpecification());
        dto.setQuantity(item.getQuantity() == null ? null : BigDecimal.valueOf(item.getQuantity()));
        dto.setUnit(item.getUnit());
        dto.setUsage(item.getUsage());
        dto.setStock(stockMap != null
                ? BigDecimal.valueOf(stockMap.getOrDefault(item.getDrugId(), 0))
                : BigDecimal.ZERO);
        return dto;
    }

    private Long parseDrugId(String drugId) {
        if (drugId == null || drugId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(drugId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public List<DispenseItemDTO> toItemDTOList(List<DispenseItem> items, Map<String, Integer> stockMap) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> toItemDTO(item, stockMap))
                .collect(Collectors.toList());
    }
}
