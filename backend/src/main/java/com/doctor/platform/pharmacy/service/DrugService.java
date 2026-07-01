package com.doctor.platform.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.pharmacy.dto.DrugResponse;
import com.doctor.platform.pharmacy.entity.Drug;
import com.doctor.platform.pharmacy.mapper.DrugMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DrugService {

    private final DrugMapper drugMapper;

    public DrugService(DrugMapper drugMapper) {
        this.drugMapper = drugMapper;
    }

    public List<DrugResponse> listDrugs(String keyword) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<Drug>()
            .eq(Drug::getStatus, 1)
            .orderByAsc(Drug::getDrugCode);

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(query -> query
                .like(Drug::getDrugCode, value)
                .or()
                .like(Drug::getDrugName, value)
            );
        }

        return drugMapper.selectList(wrapper).stream()
            .map(drug -> DrugResponse.builder()
                .drugId(drug.getDrugId())
                .code(drug.getDrugCode())
                .name(drug.getDrugName())
                .spec(drug.getSpecification())
                .price(drug.getSalePrice())
                .factory(drug.getManufacturer())
                .build())
            .toList();
    }
}
