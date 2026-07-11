package com.neuCloudBrainMedical.admin.service.schedule;

import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.entity.schedule.DoctorSchedule;
import com.neuCloudBrainMedical.admin.service.schedule.converter.ScheduleResponseAssembler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleResponseAssemblerTests {

	private final ScheduleResponseAssembler assembler = new ScheduleResponseAssembler();

	@Test
	void shouldAssembleResponseWithAssociatedDataAndRegistrationCount() {
		DoctorSchedule schedule = schedule();
		Doctor doctor = new Doctor();
		doctor.setDoctorId(10L);
		doctor.setUserId(100L);
		doctor.setDoctorNo("D001");
		doctor.setDoctorType("OUTPATIENT");
		doctor.setTitle("Chief");
		doctor.setSpecialty("Heart");
		Department department = new Department();
		department.setDeptId(20L);
		department.setDeptName("Cardiology");
		UserInfo user = new UserInfo();
		user.setRealName("Dr A");

		ScheduleResponse response = assembler.toResponse(
				schedule,
				Map.of(10L, doctor),
				Map.of(20L, department),
				Map.of(100L, user),
				Map.of(1L, 4)
		);

		assertEquals("Dr A", response.getDoctorName());
		assertEquals("Cardiology", response.getDepartmentName());
		assertEquals(4, response.getCurrentAppointments());
	}

	@Test
	void shouldFallbackWhenAssociatedDataMissing() {
		ScheduleResponse response = assembler.toResponse(schedule(), Map.of(), Map.of(), Map.of(), null);

		assertEquals("", response.getDoctorName());
		assertEquals("", response.getDepartmentName());
		assertEquals(2, response.getCurrentAppointments());
	}

	private DoctorSchedule schedule() {
		DoctorSchedule schedule = new DoctorSchedule();
		schedule.setScheduleId(1L);
		schedule.setDoctorId(10L);
		schedule.setDeptId(20L);
		schedule.setWorkDate(LocalDate.now());
		schedule.setTimePeriod("MORNING");
		schedule.setTotalQuota(10);
		schedule.setRemainQuota(8);
		schedule.setSource("MANUAL");
		schedule.setRegistrationFee(new BigDecimal("20.00"));
		schedule.setStatus("OPEN");
		return schedule;
	}
}
