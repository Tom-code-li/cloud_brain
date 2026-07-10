package com.neu.patient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.DoctorSchedule;
import java.util.List;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    @Select("SELECT * FROM doctor_schedule WHERE doctor_id = #{doctorId} AND work_date >= CURDATE() AND status IN ('可预约', 'active', '约满', '停用') ORDER BY work_date, time_period")
    List<DoctorSchedule> findFutureByDoctorId(Long doctorId);

    @Select("SELECT * FROM doctor_schedule WHERE dept_id = #{deptId} AND work_date >= CURDATE() AND status IN ('可预约', 'active', '约满', '停用') ORDER BY work_date, time_period")
    List<DoctorSchedule> findFutureByDeptId(Long deptId);

    @Select("SELECT COUNT(*) FROM doctor_schedule WHERE work_date >= CURDATE() AND status = '" + EnumValues.SCHEDULE_AVAILABLE + "' AND remain_quota > 0")
    int countFutureAvailableSchedules();

    @Select("SELECT * FROM doctor_schedule WHERE doctor_id IS NOT NULL AND dept_id IS NOT NULL ORDER BY work_date DESC, schedule_id DESC")
    List<DoctorSchedule> findScheduleRepairTemplates();
}
