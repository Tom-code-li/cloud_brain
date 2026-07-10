package com.neu.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.patient.entity.Drug;
import com.neu.patient.mapper.DrugMapper;
import com.neu.patient.service.DrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugServiceImpl implements DrugService {
    @Autowired private DrugMapper drugMapper;
    @Override public Drug getDrugById(Long drugId) { return drugMapper.selectById(drugId); }
    @Override
    public List<Drug> searchDrugs(String keyword) {
        QueryWrapper<Drug> qw = new QueryWrapper<>();
        qw.like("drug_name", keyword).or().like("drug_code", keyword);
        qw.eq("status", 1);
        return drugMapper.selectList(qw);
    }
}
