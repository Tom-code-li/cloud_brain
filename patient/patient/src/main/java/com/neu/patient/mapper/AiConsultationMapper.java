package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.AiConsultation;
import java.util.List;

@Mapper
public interface AiConsultationMapper extends BaseMapper<AiConsultation> {
    @Select("SELECT ac.*, d.dept_name AS recommendedDeptName " +
            "FROM ai_consultation ac " +
            "LEFT JOIN department d ON d.dept_id = ac.recommended_dept_id " +
            "WHERE ac.patient_id = #{patientId} ORDER BY ac.created_at DESC")
    List<AiConsultation> findByPatientId(Long patientId);
}
