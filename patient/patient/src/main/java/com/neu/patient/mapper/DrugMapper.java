package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.Drug;
import java.util.List;

@Mapper
public interface DrugMapper extends BaseMapper<Drug> {
    @Select("SELECT * FROM drug WHERE status = 1")
    List<Drug> findAllActive();
}
