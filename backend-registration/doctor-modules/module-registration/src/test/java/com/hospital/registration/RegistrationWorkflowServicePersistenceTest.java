package com.hospital.registration;

import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.repository.RegistrationJdbcRepository;
import com.hospital.registration.service.RegistrationWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationWorkflowServicePersistenceTest {
    private JdbcTemplate jdbcTemplate;
    private RegistrationWorkflowService service;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:registration_service;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dataSource.setDriverClassName("org.h2.Driver");
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();
        seedCatalog();
        service = new RegistrationWorkflowService(new RegistrationJdbcRepository(jdbcTemplate));
    }

    @Test
    void workflowServiceUsesJdbcRepositoryWhenProvided() {
        PatientView patient = service.syncPatient(new PatientSyncRequest(
                "赵持久",
                "男",
                "110101198812120031",
                "13700000003",
                "",
                ""
        ));

        assertThat(service.listDepartments())
                .extracting(department -> department.deptId())
                .containsExactly(903L);

        RegistrationView registration = service.submitOfflineRegistration(patient.patientId(), 902L, 901L);
        RegistrationView charged = service.chargeRegistration(registration.registrationId(), "现金");

        assertThat(charged.queueNo()).isEqualTo(1);
        assertThat(service.listQueue(902L, 901L))
                .extracting(RegistrationView::registrationId)
                .containsExactly(registration.registrationId());
        assertThat(jdbcTemplate.queryForObject("select remain_quota from doctor_schedule where schedule_id = 901", Integer.class))
                .isEqualTo(4);
    }

    private void createSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS fee_order_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS fee_order");
        jdbcTemplate.execute("DROP TABLE IF EXISTS registration");
        jdbcTemplate.execute("DROP TABLE IF EXISTS doctor_schedule");
        jdbcTemplate.execute("DROP TABLE IF EXISTS doctor");
        jdbcTemplate.execute("DROP TABLE IF EXISTS department");
        jdbcTemplate.execute("DROP TABLE IF EXISTS patient");
        jdbcTemplate.execute("DROP TABLE IF EXISTS sys_user");

        jdbcTemplate.execute("""
                CREATE TABLE sys_user (
                  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  role_id BIGINT,
                  username VARCHAR(80),
                  password VARCHAR(255),
                  real_name VARCHAR(80),
                  status TINYINT DEFAULT 1
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE patient (
                  patient_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  patient_no VARCHAR(40) UNIQUE NOT NULL,
                  patient_name VARCHAR(80) NOT NULL,
                  gender VARCHAR(10),
                  id_card VARCHAR(30) UNIQUE,
                  phone VARCHAR(30),
                  allergy_history TEXT,
                  past_history TEXT,
                  status TINYINT DEFAULT 1
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE department (
                  dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  dept_code VARCHAR(40) UNIQUE NOT NULL,
                  dept_name VARCHAR(100) NOT NULL,
                  dept_type VARCHAR(40) NOT NULL,
                  location VARCHAR(120),
                  description TEXT,
                  status TINYINT DEFAULT 1
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE doctor (
                  doctor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT,
                  dept_id BIGINT,
                  doctor_no VARCHAR(40),
                  doctor_type VARCHAR(40),
                  title VARCHAR(80),
                  specialty TEXT,
                  status TINYINT DEFAULT 1
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE doctor_schedule (
                  schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  doctor_id BIGINT,
                  dept_id BIGINT,
                  work_date DATE,
                  time_period VARCHAR(20),
                  start_time TIME,
                  end_time TIME,
                  total_quota INT DEFAULT 0,
                  remain_quota INT DEFAULT 0,
                  registration_fee DECIMAL(10,2),
                  status VARCHAR(30)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE registration (
                  registration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  patient_id BIGINT,
                  dept_id BIGINT,
                  doctor_id BIGINT,
                  schedule_id BIGINT,
                  source VARCHAR(30),
                  registration_no VARCHAR(50) UNIQUE NOT NULL,
                  queue_no INT,
                  registration_fee DECIMAL(10,2),
                  fee_status VARCHAR(30),
                  status VARCHAR(30),
                  registered_at DATETIME
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE fee_order (
                  fee_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  order_no VARCHAR(50) UNIQUE NOT NULL,
                  patient_id BIGINT,
                  registration_id BIGINT,
                  business_type VARCHAR(40),
                  business_id BIGINT,
                  total_amount DECIMAL(10,2),
                  paid_amount DECIMAL(10,2),
                  refund_amount DECIMAL(10,2),
                  status VARCHAR(30),
                  paid_at DATETIME
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE fee_order_item (
                  fee_order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  fee_order_id BIGINT,
                  item_type VARCHAR(40),
                  item_id BIGINT,
                  item_name VARCHAR(120),
                  unit_price DECIMAL(10,2),
                  quantity INT,
                  amount DECIMAL(10,2),
                  status VARCHAR(30)
                )
                """);
    }

    private void seedCatalog() {
        jdbcTemplate.update("insert into sys_user (user_id, role_id, username, password, real_name, status) values (902, 2, 'persist001', '{noop}123456', '孙持久', 1)");
        jdbcTemplate.update("insert into department (dept_id, dept_code, dept_name, dept_type, location, description, status) values (903, 'PERSIST', '持久化门诊', 'OUTPATIENT', '门诊三楼', '服务层持久化测试科室', 1)");
        jdbcTemplate.update("insert into doctor (doctor_id, user_id, dept_id, doctor_no, doctor_type, title, specialty, status) values (902, 902, 903, 'D-PERSIST-001', 'OUTPATIENT', '主任医师', '持久化挂号流程', 1)");
        jdbcTemplate.update("insert into doctor_schedule (schedule_id, doctor_id, dept_id, work_date, time_period, start_time, end_time, total_quota, remain_quota, registration_fee, status) values (901, 902, 903, '2026-06-22', '上午', '08:00:00', '12:00:00', 5, 5, 22.00, '可预约')");
    }
}
