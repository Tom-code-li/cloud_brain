package com.neuCloudBrainMedical.admin.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.neuCloudBrainMedical.admin.AdminApplication;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;

class MapperDataSourceRoutingTest {

	@Test
	void adminAndBizServicesReadFromDifferentDataSources() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AdminApplication.class)
				.profiles("dual-ds-test")
				.properties("spring.main.web-application-type=none")
				.run()) {
			DataSource adminDataSource = context.getBean("adminDataSource", DataSource.class);
			DataSource bizDataSource = context.getBean("bizDataSource", DataSource.class);
			JdbcTemplate adminJdbc = new JdbcTemplate(adminDataSource);
			JdbcTemplate bizJdbc = new JdbcTemplate(bizDataSource);

			adminJdbc.execute("DROP TABLE IF EXISTS sys_role");
			adminJdbc.execute("""
					CREATE TABLE sys_role (
					  role_id BIGINT PRIMARY KEY,
					  role_code VARCHAR(50),
					  role_name VARCHAR(50),
					  description VARCHAR(255),
					  status INT,
					  created_at TIMESTAMP,
					  updated_at TIMESTAMP
					)
					""");
			adminJdbc.update(
					"INSERT INTO sys_role(role_id, role_code, role_name, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
					1L, "ADMIN", "Administrator", "admin role", 1, Timestamp.valueOf(LocalDateTime.now()),
					Timestamp.valueOf(LocalDateTime.now()));
			adminJdbc.update(
					"INSERT INTO sys_role(role_id, role_code, role_name, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
					2L, "DOCTOR", "Doctor", "doctor role", 1, Timestamp.valueOf(LocalDateTime.now()),
					Timestamp.valueOf(LocalDateTime.now()));

			bizJdbc.execute("DROP TABLE IF EXISTS registration");
			bizJdbc.execute("""
					CREATE TABLE registration (
					  registration_id BIGINT PRIMARY KEY,
					  patient_id BIGINT,
					  consultation_id BIGINT,
					  dept_id BIGINT,
					  doctor_id BIGINT,
					  schedule_id BIGINT,
					  operator_user_id BIGINT,
					  source VARCHAR(20),
					  registration_no VARCHAR(50),
					  queue_no INT,
					  registration_fee DECIMAL(10,2),
					  fee_status VARCHAR(20),
					  status VARCHAR(30),
					  registered_at TIMESTAMP,
					  called_at TIMESTAMP,
					  created_at TIMESTAMP,
					  updated_at TIMESTAMP
					)
					""");
			bizJdbc.update("""
					INSERT INTO registration(
					  registration_id, patient_id, consultation_id, dept_id, doctor_id, schedule_id, operator_user_id,
					  source, registration_no, queue_no, registration_fee, fee_status, status, registered_at, called_at,
					  created_at, updated_at
					) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
					""",
					1L, 10L, null, 20L, 30L, 40L, null, "线上", "REG-001", 1, 15.00, "已支付", "已预约",
					Timestamp.valueOf(LocalDateTime.now()), null, Timestamp.valueOf(LocalDateTime.now()),
					Timestamp.valueOf(LocalDateTime.now()));

			IRoleQueryService roleQueryService = context.getBean(IRoleQueryService.class);
			IScheduleQueryService scheduleQueryService = context.getBean(IScheduleQueryService.class);

			List<SysRoleResponse> roles = roleQueryService.listRoles();
			long registrations = scheduleQueryService.countRegistrationsByTimeRange(
					LocalDateTime.now().minusDays(1),
					LocalDateTime.now().plusDays(1));

			assertThat(roles).hasSize(2);
			assertThat(registrations).isEqualTo(1);
		}
	}
}
