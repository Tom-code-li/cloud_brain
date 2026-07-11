package com.neuCloudBrainMedical.admin.mapper.admin.schedule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班 Mapper（排班模块内部使用）。
 *
 * <p>跨模块 Service 不应直接依赖本接口，请通过
 * {@link com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService}
 * 或 {@link com.neuCloudBrainMedical.admin.service.schedule.IScheduleCommandService}
 * 访问排班数据。</p>
 */
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {

	@Select("<script>"
			+ "SELECT COUNT(DISTINCT doctor_id) FROM doctor_schedule "
			+ "WHERE work_date = #{workDate} AND status IN "
			+ "<foreach item='s' collection='statuses' open='(' separator=',' close=')'>#{s}</foreach>"
			+ "</script>")
	long countDistinctDoctorsByWorkDateAndStatuses(@Param("workDate") LocalDate workDate,
			@Param("statuses") List<String> statuses);
}
