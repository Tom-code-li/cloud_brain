package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.PrescriptionItem;
import java.util.List;

@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
    @Select("SELECT * FROM prescription_item WHERE prescription_id = #{prescriptionId}")
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
}
