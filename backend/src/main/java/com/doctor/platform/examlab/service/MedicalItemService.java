package com.doctor.platform.examlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.examlab.dto.MedicalItemResponse;
import com.doctor.platform.examlab.entity.MedicalItem;
import com.doctor.platform.examlab.mapper.MedicalItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MedicalItemService {

    private static final String EXAM_TYPE = "检查";
    private static final String LAB_TYPE = "检验";

    private final MedicalItemMapper medicalItemMapper;

    public MedicalItemService(MedicalItemMapper medicalItemMapper) {
        this.medicalItemMapper = medicalItemMapper;
    }

    public List<MedicalItemResponse> listExamItems(String keyword) {
        return listItems(EXAM_TYPE, keyword);
    }

    public List<MedicalItemResponse> listLabItems(String keyword) {
        return listItems(LAB_TYPE, keyword);
    }

    private List<MedicalItemResponse> listItems(String itemType, String keyword) {
        LambdaQueryWrapper<MedicalItem> wrapper = new LambdaQueryWrapper<MedicalItem>()
            .eq(MedicalItem::getItemType, itemType)
            .eq(MedicalItem::getStatus, 1)
            .orderByAsc(MedicalItem::getItemCode);

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(query -> query
                .like(MedicalItem::getItemCode, value)
                .or()
                .like(MedicalItem::getItemName, value)
            );
        }

        return medicalItemMapper.selectList(wrapper).stream()
            .map(this::toResponse)
            .toList();
    }

    private MedicalItemResponse toResponse(MedicalItem item) {
        return MedicalItemResponse.builder()
            .id(item.getItemId())
            .code(item.getItemCode())
            .name(item.getItemName())
            .type(item.getItemType())
            .spec(StringUtils.hasText(item.getUnit()) ? item.getUnit() : "次")
            .price(item.getPrice())
            .feeType(item.getItemType() + "费")
            .build();
    }
}
