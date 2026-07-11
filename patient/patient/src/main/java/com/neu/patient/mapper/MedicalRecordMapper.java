package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.MedicalRecord;
import java.util.List;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
    @Select("SELECT * FROM medical_record WHERE patient_id = #{patientId} ORDER BY created_at DESC")
    List<MedicalRecord> findByPatientId(Long patientId);
}
