package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.ExamLabReport;
import java.util.List;

@Mapper
public interface ExamLabReportMapper extends BaseMapper<ExamLabReport> {
    @Select("SELECT * FROM exam_lab_report WHERE patient_id = #{patientId} ORDER BY created_at DESC")
    List<ExamLabReport> findByPatientId(Long patientId);
}
