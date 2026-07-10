package com.neuCloudBrainMedical.admin.service.schedule.converter;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 排班 Entity → DTO 装配组件。
 * 单一职责：将 DoctorSchedule 与关联数据（医生、科室、用户、挂号数）聚合为 ScheduleResponse。
 */
@Component
public class ScheduleResponseAssembler {

	/**
	 * 将 DoctorSchedule 装配为 ScheduleResponse。
	 *
	 * @param schedule           排班实体
	 * @param doctors            医生 id -> 医生实体 的映射
	 * @param departments        科室 id -> 科室实体 的映射
	 * @param users              user_id -> UserInfo DTO 的映射（用来取医生姓名 realName）
	 * @param registrationCounts 排班 id -> 真实挂号数 的映射（可为 null，缺失时回退到剩余配额计算）
	 */
	public ScheduleResponse toResponse(DoctorSchedule schedule,
	                                    Map<Long, Doctor> doctors,
	                                    Map<Long, Department> departments,
	                                    Map<Long, UserInfo> users,
	                                    Map<Long, Integer> registrationCounts) {
		Doctor doctor = doctors.get(schedule.getDoctorId());
		Department department = departments.get(schedule.getDeptId());

		String doctorName = "";
		if (doctor != null && users != null) {
			UserInfo user = users.get(doctor.getUserId());
			if (user != null) {
				doctorName = user.getRealName();
			}
		}

		int realCount;
		if (registrationCounts != null && registrationCounts.containsKey(schedule.getScheduleId())) {
			realCount = registrationCounts.get(schedule.getScheduleId());
		} else {
			realCount = schedule.getTotalQuota() - schedule.getRemainQuota();
		}

		ScheduleResponse response = new ScheduleResponse();
		response.setId(schedule.getScheduleId());
		response.setDoctorId(schedule.getDoctorId());
		response.setDoctorName(doctorName);
		response.setDoctorNo(doctor != null ? doctor.getDoctorNo() : "");
		response.setDoctorType(doctor != null ? doctor.getDoctorType() : "");
		response.setTitle(doctor != null ? doctor.getTitle() : "");
		response.setSpecialty(doctor != null ? doctor.getSpecialty() : "");
		response.setDepartmentId(schedule.getDeptId());
		response.setDepartmentName(department != null ? department.getDeptName() : "");
		response.setScheduleDate(schedule.getWorkDate());
		response.setTimeSlot(schedule.getTimePeriod());
		response.setMaxAppointments(schedule.getTotalQuota());
		response.setCurrentAppointments(realCount);
		response.setSource(schedule.getSource());
		response.setRegistrationFee(schedule.getRegistrationFee());
		response.setStatus(schedule.getStatus());
		return response;
	}
}