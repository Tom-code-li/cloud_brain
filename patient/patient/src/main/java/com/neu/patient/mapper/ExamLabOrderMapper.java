package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.ExamLabOrder;
import java.util.List;

@Mapper
public interface ExamLabOrderMapper extends BaseMapper<ExamLabOrder> {
    @Select("SELECT * FROM exam_lab_order WHERE patient_id = #{patientId} ORDER BY created_at DESC")
    List<ExamLabOrder> findByPatientId(Long patientId);
}
