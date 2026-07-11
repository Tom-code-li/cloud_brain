package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.Registration;
import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    @Select("SELECT r.*, u.real_name AS doctorName, d.dept_name AS deptName " +
            "FROM registration r " +
            "LEFT JOIN doctor doc ON r.doctor_id = doc.doctor_id " +
            "LEFT JOIN sys_user u ON doc.user_id = u.user_id " +
            "LEFT JOIN department d ON r.dept_id = d.dept_id " +
            "WHERE r.patient_id = #{patientId} ORDER BY r.created_at DESC")
    List<Registration> findByPatientId(Long patientId);
    
    @Select("SELECT r.*, u.real_name AS doctorName, d.dept_name AS deptName " +
            "FROM registration r " +
            "LEFT JOIN doctor doc ON r.doctor_id = doc.doctor_id " +
            "LEFT JOIN sys_user u ON doc.user_id = u.user_id " +
            "LEFT JOIN department d ON r.dept_id = d.dept_id " +
            "WHERE r.patient_id = #{patientId} AND r.status IN ('待支付', '待确认') ORDER BY r.registered_at DESC")
    List<Registration> findActiveByPatientId(Long patientId);
}
