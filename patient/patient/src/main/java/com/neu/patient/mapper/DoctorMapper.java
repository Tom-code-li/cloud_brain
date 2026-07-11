package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.Doctor;
import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    @Select("SELECT d.*, u.real_name AS realName, COALESCE(q.available_quota, 0) AS availableQuota, " +
            "CASE WHEN d.status <> 1 THEN '停用' " +
            "WHEN COALESCE(q.available_quota, 0) > 0 THEN '可预约' ELSE '约满' END AS appointmentStatus " +
            "FROM doctor d " +
            "LEFT JOIN sys_user u ON d.user_id = u.user_id " +
            "LEFT JOIN (" +
            "  SELECT doctor_id, SUM(remain_quota) AS available_quota " +
            "  FROM doctor_schedule " +
            "  WHERE work_date >= CURDATE() AND status IN ('可预约', 'active') AND remain_quota > 0 " +
            "  GROUP BY doctor_id" +
            ") q ON d.doctor_id = q.doctor_id " +
            "WHERE d.dept_id = #{deptId} " +
            "ORDER BY d.status DESC, COALESCE(q.available_quota, 0) > 0 DESC, COALESCE(q.available_quota, 0) DESC, d.doctor_id")
    List<Doctor> findByDeptId(Long deptId);
    
    @Select("SELECT d.*, u.real_name AS realName FROM doctor d LEFT JOIN sys_user u ON d.user_id = u.user_id WHERE d.status = 1")
    List<Doctor> findAllActive();
}
