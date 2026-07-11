package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.Prescription;
import java.util.List;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
    @Select("SELECT * FROM prescription WHERE patient_id = #{patientId} ORDER BY created_at DESC")
    List<Prescription> findByPatientId(Long patientId);
}
