package com.hospital.registration;

import com.hospital.common.core.BusinessException;
import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.repository.RegistrationJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationJdbcRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private RegistrationJdbcRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:registration;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dataSource.setDriverClassName("org.h2.Driver");
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new RegistrationJdbcRepository(jdbcTemplate);
        createSchema();
        seedCatalog();
    }

    @Test
    void syncPatientPersistsAndReusesByIdCard() {
        PatientView first = repository.syncPatient(new PatientSyncRequest(
                "张晓雨",
                "女",
                "110101199204180021",
                "13800000001",
                "青霉素过敏",
                "无特殊既往史"
        ));

        PatientView second = repository.syncPatient(new PatientSyncRequest(
                "张晓雨",
                "女",
                "110101199204180021",
                "13899990000",
                "青霉素过敏",
                "无特殊既往史"
        ));

        assertThat(second.patientId()).isEqualTo(first.patientId());
        assertThat(second.phone()).isEqualTo("13899990000");
        assertThat(jdbcTemplate.queryForObject("select count(*) from patient", Integer.class)).isEqualTo(1);
    }

    @Test
    void syncPatientValidatesRequiredFieldsAndReusesByNameAndPhone() {
        assertThatThrownBy(() -> repository.syncPatient(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> repository.syncPatient(new PatientSyncRequest(
                " ",
                "male",
                "110101199001010011",
                "13800000001",
                "",
                ""
        ))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> repository.syncPatient(new PatientSyncRequest(
                "patient",
                "male",
                " ",
                " ",
                "",
                ""
        ))).isInstanceOf(BusinessException.class);

        PatientView first = repository.syncPatient(new PatientSyncRequest(
                "phone patient",
                "female",
                "",
                "13800000002",
                "",
                ""
        ));
        PatientView second = repository.syncPatient(new PatientSyncRequest(
                "phone patient",
                "female",
                "",
                "13800000002",
                "allergy",
                "history"
        ));

        assertThat(second.patientId()).isEqualTo(first.patientId());
        assertThat(second.allergyHistory()).isEqualTo("allergy");
        assertThat(second.pastHistory()).isEqualTo("history");
    }

    @Test
    void catalogQueriesReturnEnabledDepartmentsDoctorsAndFilteredSchedules() {
        jdbcTemplate.update("insert into department (dept_id, dept_code, dept_name, dept_type, location, description, status) values (99, 'OFF', 'disabled', 'OUTPATIENT', 'N/A', 'disabled', 0)");
        jdbcTemplate.update("insert into doctor_schedule (schedule_id, doctor_id, dept_id, work_date, time_period, start_time, end_time, total_quota, remain_quota, registration_fee, status) values (2, 2, 2, '2026-07-02', 'afternoon', '14:00:00', '18:00:00', 20, 0, 15.00, '可预约')");
        jdbcTemplate.update("insert into doctor_schedule (schedule_id, doctor_id, dept_id, work_date, time_period, start_time, end_time, total_quota, remain_quota, registration_fee, status) values (3, 2, 2, '2026-07-03', 'morning', '08:00:00', '12:00:00', 20, 10, 15.00, '停诊')");

        List<DepartmentView> departments = repository.listDepartments();
        List<DoctorView> allDoctors = repository.listDoctors(null);
        List<DoctorView> deptDoctors = repository.listDoctors(2L);
        List<DoctorView> noDoctors = repository.listDoctors(999L);
        List<DoctorScheduleView> allSchedules = repository.listSchedules(null, null, null);
        List<DoctorScheduleView> filteredSchedules = repository.listSchedules(2L, 2L, LocalDate.of(2026, 6, 22));
        List<DoctorScheduleView> noSchedules = repository.listSchedules(2L, 2L, LocalDate.of(2026, 7, 2));

        assertThat(departments).extracting(DepartmentView::deptId).contains(2L, 16L).doesNotContain(99L);
        assertThat(allDoctors).extracting(DoctorView::doctorId).contains(2L, 16L);
        assertThat(deptDoctors).extracting(DoctorView::doctorId).containsExactly(2L);
        assertThat(noDoctors).isEmpty();
        assertThat(allSchedules).extracting(DoctorScheduleView::scheduleId).containsExactly(1L);
        assertThat(filteredSchedules).extracting(DoctorScheduleView::scheduleId).containsExactly(1L);
        assertThat(noSchedules).isEmpty();
    }

    @Test
    void offlineRegistrationPersistsFeeOrderAndChargeMovesIntoQueue() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "刘建国",
                "男",
                "110101197809030039",
                "13900000002",
                "",
                "高血压病史5年"
        ));

        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        List<FeeOrderView> pendingFees = repository.listPendingFees(patient.patientId(), registration.registrationId());

        assertThat(pendingFees).singleElement().satisfies(fee -> {
            assertThat(fee.businessId()).isEqualTo(registration.registrationId());
            assertThat(fee.feeType()).isEqualTo("挂号费");
            assertThat(fee.payStatus()).isEqualTo("待支付");
        });

        RegistrationView charged = repository.chargeRegistration(registration.registrationId(), "现金");

        assertThat(charged.status()).isEqualTo("待接诊");
        assertThat(charged.queueNo()).isEqualTo(1);
        assertThat(repository.listQueue(2L, 1L))
                .extracting(RegistrationView::registrationId)
                .containsExactly(registration.registrationId());
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from outpatient_visit where registration_id = ? and status = ?",
                Integer.class,
                registration.registrationId(),
                "待接诊"
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select remain_quota from doctor_schedule where schedule_id = 1", Integer.class))
                .isEqualTo(37);
    }

    @Test
    void onlineConfirmationCreatesOutpatientVisitBeforeCall() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "周明",
                "男",
                "110101198802020041",
                "13900000003",
                "",
                ""
        ));

        RegistrationView online = repository.createOnlineRegistration(patient.patientId(), 2L, 1L, true);

        assertThat(repository.listOnlinePending())
                .extracting(RegistrationView::registrationId)
                .containsExactly(online.registrationId());

        RegistrationView confirmed = repository.confirmOnlineRegistration(online.registrationId());
        assertThat(confirmed.status()).isEqualTo("待接诊");
        assertThat(confirmed.queueNo()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from outpatient_visit where registration_id = ? and status = ?",
                Integer.class,
                online.registrationId(),
                "待接诊"
        )).isEqualTo(1);

        RegistrationView called = repository.callNext(2L, 1L);
        assertThat(called.registrationId()).isEqualTo(online.registrationId());
        assertThat(called.status()).isEqualTo("接诊中");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from outpatient_visit where registration_id = ?",
                Integer.class,
                online.registrationId()
        )).isEqualTo(1);
    }

    @Test
    void patientRegistrationAndScheduleErrorsSurfaceAsBusinessExceptions() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "error patient",
                "male",
                "110101199101010101",
                "13900000101",
                "",
                ""
        ));

        assertThat(repository.findPatient(patient.patientId()).patientName()).isEqualTo("error patient");
        assertThatThrownBy(() -> repository.findPatient(99999L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> repository.createOfflineRegistration(99999L, 2L, 1L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> repository.createOfflineRegistration(patient.patientId(), 999L, 1L))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> repository.createOfflineRegistration(patient.patientId(), 2L, 99999L))
                .isInstanceOf(BusinessException.class);

        jdbcTemplate.update("update doctor_schedule set remain_quota = 0 where schedule_id = 1");
        assertThatThrownBy(() -> repository.createOfflineRegistration(patient.patientId(), 2L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void onlineConfirmationRejectsOfflineAndUnpaidOnlineRegistrations() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "online edge",
                "female",
                "110101199101010102",
                "13900000102",
                "",
                ""
        ));
        RegistrationView offline = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        repository.chargeRegistration(offline.registrationId(), "cash");
        RegistrationView unpaidOnline = repository.createOnlineRegistration(patient.patientId(), 2L, 1L, false);

        assertThatThrownBy(() -> repository.confirmOnlineRegistration(offline.registrationId()))
                .isInstanceOf(BusinessException.class);
        assertThat(repository.listOnlinePending())
                .extracting(RegistrationView::registrationId)
                .contains(unpaidOnline.registrationId());
        assertThatThrownBy(() -> repository.confirmOnlineRegistration(unpaidOnline.registrationId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void queuePendingFeeAndHistoryQueriesSupportFilters() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "query patient",
                "male",
                "110101199101010103",
                "13900000103",
                "",
                ""
        ));
        RegistrationView first = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        RegistrationView second = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);

        assertThat(repository.listPendingFees(null, null))
                .extracting(FeeOrderView::registrationId)
                .contains(first.registrationId(), second.registrationId());
        assertThat(repository.listPendingFees(patient.patientId(), second.registrationId()))
                .singleElement()
                .extracting(FeeOrderView::registrationId)
                .isEqualTo(second.registrationId());

        RegistrationView charged = repository.chargeRegistration(first.registrationId(), "cash");
        FeeHistoryView allHistory = repository.feeHistory(null, null);
        FeeHistoryView registrationHistory = repository.feeHistory(patient.patientId(), first.registrationId());

        assertThat(repository.listQueue(null, null))
                .extracting(RegistrationView::registrationId)
                .containsExactly(charged.registrationId());
        assertThat(repository.listQueue(2L, 1L, LocalDate.of(2026, 6, 22)))
                .extracting(RegistrationView::registrationId)
                .containsExactly(charged.registrationId());
        assertThat(repository.listQueue(2L, 1L, LocalDate.of(2026, 7, 2))).isEmpty();
        assertThat(repository.callNext(null, null).registrationId()).isEqualTo(charged.registrationId());
        assertThatThrownBy(() -> repository.callNext(2L, 1L, LocalDate.of(2026, 7, 2)))
                .isInstanceOf(BusinessException.class);
        assertThat(allHistory.orders()).isNotEmpty();
        assertThat(registrationHistory.orders())
                .extracting(FeeOrderView::registrationId)
                .containsExactly(first.registrationId());
    }

    @Test
    void chargingRegistrationIsIdempotentAndRefundedRegistrationCannotBeRecharged() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "idempotent patient",
                "male",
                "110101199101010104",
                "13900000104",
                "",
                ""
        ));
        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        RegistrationView firstCharge = repository.chargeRegistration(registration.registrationId(), "cash");
        RegistrationView secondCharge = repository.chargeRegistration(registration.registrationId(), "cash");
        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_id = ?",
                Long.class,
                registration.registrationId()
        );

        assertThat(secondCharge.queueNo()).isEqualTo(firstCharge.queueNo());
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from payment_record where fee_order_id = ?",
                Integer.class,
                feeOrderId
        )).isEqualTo(1);

        repository.refund(feeOrderId, "cancel");
        RegistrationView chargedAfterRefund = repository.chargeRegistration(registration.registrationId(), "cash");
        assertThat(chargedAfterRefund.registrationId()).isEqualTo(registration.registrationId());
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from payment_record where fee_order_id = ?",
                Integer.class,
                feeOrderId
        )).isEqualTo(1);
        assertThat(repository.listQueue(2L, 1L))
                .extracting(RegistrationView::registrationId)
                .doesNotContain(registration.registrationId());
    }

    @Test
    void chargeFeeOrderRejectsMissingAndRefundedOrders() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "fee edge",
                "female",
                "110101199101010105",
                "13900000105",
                "",
                ""
        ));
        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_id = ?",
                Long.class,
                registration.registrationId()
        );

        assertThatThrownBy(() -> repository.chargeFeeOrder(99999L, "cash"))
                .isInstanceOf(BusinessException.class);
        repository.refund(feeOrderId, "cancel");
        assertThatThrownBy(() -> repository.chargeFeeOrder(feeOrderId, "cash"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void chargeAndRefundWritePaymentAndRefundRecords() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "赵琳",
                "女",
                "110101199603030052",
                "13900000004",
                "",
                ""
        ));

        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        repository.chargeRegistration(registration.registrationId(), "现金");

        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_id = ?",
                Long.class,
                registration.registrationId()
        );
        assertThat(jdbcTemplate.queryForObject(
                "select payment_method from payment_record where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("现金");

        assertThat(repository.checkRefund(feeOrderId).refundable()).isTrue();
        FeeOrderView refunded = repository.refund(feeOrderId, "患者取消就诊");

        assertThat(refunded.payStatus()).isEqualTo("已退费");
        assertThat(jdbcTemplate.queryForObject(
                "select reason from refund_record where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("患者取消就诊");
        assertThat(jdbcTemplate.queryForObject("select remain_quota from doctor_schedule where schedule_id = 1", Integer.class))
                .isEqualTo(38);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from outpatient_visit where registration_id = ?",
                Integer.class,
                registration.registrationId()
        )).isZero();
    }

    @Test
    void refundPaidFeeOrderWithoutPaymentRecordUsesNullablePaymentId() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "钱退款",
                "男",
                "110101199707070071",
                "13900000071",
                "",
                ""
        ));
        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_id = ?",
                Long.class,
                registration.registrationId()
        );
        jdbcTemplate.update("update fee_order set status = ?, paid_amount = total_amount, paid_at = current_timestamp where fee_order_id = ?",
                "已支付",
                feeOrderId
        );
        jdbcTemplate.update("update registration set fee_status = ?, status = ?, queue_no = ? where registration_id = ?",
                "已支付",
                "待接诊",
                9,
                registration.registrationId()
        );

        FeeOrderView refunded = repository.refund(feeOrderId, "患者取消就诊");

        assertThat(refunded.payStatus()).isEqualTo("已退费");
        assertThat(jdbcTemplate.queryForObject(
                "select payment_id from refund_record where fee_order_id = ?",
                Long.class,
                feeOrderId
        )).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "select reason from refund_record where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("患者取消就诊");
    }

    @Test
    void chargeFeeOrderSupportsExamLabBusinessOrder() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "吴检验",
                "男",
                "110101199101010088",
                "13900000088",
                "",
                ""
        ));
        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        jdbcTemplate.update("""
                        insert into exam_lab_order (
                            order_id, patient_id, fee_status, status
                        ) values (?, ?, ?, ?)
                        """,
                7001L,
                patient.patientId(),
                "待支付",
                "待缴费"
        );
        jdbcTemplate.update("""
                        insert into exam_lab_order_item (
                            order_item_id, order_id, item_name, amount, status
                        ) values (?, ?, ?, ?, ?)
                        """,
                7101L,
                7001L,
                "血常规",
                30.00,
                "待支付"
        );
        jdbcTemplate.update("""
                        insert into fee_order (
                            order_no, patient_id, registration_id, business_type, business_id,
                            total_amount, paid_amount, refund_amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                "FEE-EXAM-7001",
                patient.patientId(),
                registration.registrationId(),
                "EXAM_LAB_ORDER",
                7001L,
                30.00,
                0.00,
                0.00,
                "待支付"
        );
        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_type = 'EXAM_LAB_ORDER'",
                Long.class
        );
        jdbcTemplate.update("""
                        insert into fee_order_item (
                            fee_order_id, item_type, item_id, item_name,
                            unit_price, quantity, amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                feeOrderId,
                "EXAM_LAB_ORDER",
                7101L,
                "血常规",
                30.00,
                1,
                30.00,
                "待支付"
        );

        FeeOrderView paid = repository.chargeFeeOrder(feeOrderId, "微信");

        assertThat(paid.feeType()).isEqualTo("检查检验费");
        assertThat(paid.payStatus()).isEqualTo("已支付");
        assertThat(jdbcTemplate.queryForObject(
                "select status from fee_order where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("已支付");
        assertThat(jdbcTemplate.queryForObject(
                "select fee_status from exam_lab_order where order_id = ?",
                String.class,
                7001L
        )).isEqualTo("已支付");
        assertThat(jdbcTemplate.queryForObject(
                "select status from exam_lab_order_item where order_item_id = ?",
                String.class,
                7101L
        )).isEqualTo("待执行");
        assertThat(jdbcTemplate.queryForObject(
                "select payment_method from payment_record where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("微信");
    }

    @Test
    void chargeFeeOrderSupportsPrescriptionAndCreatesPharmacyDispense() {
        PatientView patient = repository.syncPatient(new PatientSyncRequest(
                "郑取药",
                "女",
                "110101199202020099",
                "13900000099",
                "",
                ""
        ));
        RegistrationView registration = repository.createOfflineRegistration(patient.patientId(), 2L, 1L);
        jdbcTemplate.update("""
                        insert into prescription (
                            prescription_id, patient_id, total_amount, fee_status, status
                        ) values (?, ?, ?, ?, ?)
                        """,
                8001L,
                patient.patientId(),
                88.00,
                "待支付",
                "待缴费"
        );
        jdbcTemplate.update("""
                        insert into prescription_item (
                            prescription_item_id, prescription_id, status
                        ) values (?, ?, ?)
                        """,
                8101L,
                8001L,
                "待发药"
        );
        jdbcTemplate.update("""
                        insert into fee_order (
                            order_no, patient_id, registration_id, business_type, business_id,
                            total_amount, paid_amount, refund_amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                "FEE-RX-8001",
                patient.patientId(),
                registration.registrationId(),
                "PRESCRIPTION",
                8001L,
                88.00,
                0.00,
                0.00,
                "待支付"
        );
        Long feeOrderId = jdbcTemplate.queryForObject(
                "select fee_order_id from fee_order where business_type = 'PRESCRIPTION'",
                Long.class
        );
        jdbcTemplate.update("""
                        insert into fee_order_item (
                            fee_order_id, item_type, item_id, item_name,
                            unit_price, quantity, amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                feeOrderId,
                "药品",
                8101L,
                "布洛芬片",
                88.00,
                1,
                88.00,
                "待支付"
        );

        FeeOrderView paid = repository.chargeFeeOrder(feeOrderId, "支付宝");

        assertThat(paid.feeType()).isEqualTo("药品费");
        assertThat(paid.payStatus()).isEqualTo("已支付");
        assertThat(jdbcTemplate.queryForObject(
                "select fee_status from prescription where prescription_id = ?",
                String.class,
                8001L
        )).isEqualTo("已支付");
        assertThat(jdbcTemplate.queryForObject(
                "select status from prescription where prescription_id = ?",
                String.class,
                8001L
        )).isEqualTo("待发药");
        assertThat(jdbcTemplate.queryForObject(
                "select status from prescription_item where prescription_item_id = ?",
                String.class,
                8101L
        )).isEqualTo("待发药");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from pharmacy_dispense where prescription_id = ? and status = ?",
                Integer.class,
                8001L,
                "待发药"
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select pharmacy_doctor_id from pharmacy_dispense where prescription_id = ?",
                Long.class,
                8001L
        )).isEqualTo(16L);
        assertThat(jdbcTemplate.queryForObject(
                "select payment_method from payment_record where fee_order_id = ?",
                String.class,
                feeOrderId
        )).isEqualTo("支付宝");
    }

    private void createSchema() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS refund_record");
        jdbcTemplate.execute("DROP TABLE IF EXISTS outpatient_visit");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pharmacy_dispense");
        jdbcTemplate.execute("DROP TABLE IF EXISTS prescription_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS prescription");
        jdbcTemplate.execute("DROP TABLE IF EXISTS payment_record");
        jdbcTemplate.execute("DROP TABLE IF EXISTS fee_order_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS fee_order");
        jdbcTemplate.execute("DROP TABLE IF EXISTS exam_lab_order_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS exam_lab_order");
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
                CREATE TABLE payment_record (
                  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  fee_order_id BIGINT,
                  payment_no VARCHAR(50) UNIQUE NOT NULL,
                  payment_method VARCHAR(30),
                  payment_amount DECIMAL(10,2),
                  payer_name VARCHAR(80),
                  status VARCHAR(30),
                  paid_at DATETIME,
                  remark VARCHAR(255)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE refund_record (
                  refund_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  fee_order_id BIGINT,
                  payment_id BIGINT,
                  refund_no VARCHAR(50) UNIQUE NOT NULL,
                  refund_type VARCHAR(30),
                  refund_amount DECIMAL(10,2),
                  reason VARCHAR(255),
                  status VARCHAR(30),
                  requested_at DATETIME,
                  completed_at DATETIME
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE outpatient_visit (
                  visit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  registration_id BIGINT UNIQUE,
                  patient_id BIGINT,
                  doctor_id BIGINT,
                  dept_id BIGINT,
                  visit_no VARCHAR(50) UNIQUE NOT NULL,
                  queue_no INT,
                  status VARCHAR(30),
                  started_at DATETIME
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE exam_lab_order (
                  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  patient_id BIGINT,
                  fee_status VARCHAR(30),
                  status VARCHAR(30)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE exam_lab_order_item (
                  order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  order_id BIGINT,
                  item_name VARCHAR(120),
                  amount DECIMAL(10,2),
                  status VARCHAR(30),
                  executed_at DATETIME
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE prescription (
                  prescription_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  patient_id BIGINT,
                  total_amount DECIMAL(10,2),
                  fee_status VARCHAR(30),
                  status VARCHAR(30)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE prescription_item (
                  prescription_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  prescription_id BIGINT,
                  status VARCHAR(30)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE pharmacy_dispense (
                  dispense_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  prescription_id BIGINT,
                  patient_id BIGINT,
                  pharmacy_doctor_id BIGINT,
                  dispense_no VARCHAR(50) UNIQUE NOT NULL,
                  total_amount DECIMAL(10,2),
                  status VARCHAR(30),
                  dispensed_at DATETIME
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
        jdbcTemplate.update("insert into sys_user (user_id, role_id, username, password, real_name, status) values (2, 2, 'out001', '{noop}123456', '王门诊', 1)");
        jdbcTemplate.update("insert into sys_user (user_id, role_id, username, password, real_name, status) values (16, 4, 'pha001', '{noop}123456', '李药师', 1)");
        jdbcTemplate.update("insert into department (dept_id, dept_code, dept_name, dept_type, location, description, status) values (2, 'GM', '全科门诊', 'OUTPATIENT', '门诊二楼', '常见病、多发病首诊', 1)");
        jdbcTemplate.update("insert into department (dept_id, dept_code, dept_name, dept_type, location, description, status) values (16, 'PHA', '门诊药房', 'PHARMACY', '门诊一楼', '门诊药品调配', 1)");
        jdbcTemplate.update("insert into doctor (doctor_id, user_id, dept_id, doctor_no, doctor_type, title, specialty, status) values (2, 2, 2, 'D-OUT-001', 'OUTPATIENT', '主治医师', '呼吸道感染、慢病随访', 1)");
        jdbcTemplate.update("insert into doctor (doctor_id, user_id, dept_id, doctor_no, doctor_type, title, specialty, status) values (16, 16, 16, 'D-PHA-001', 'PHARMACY', '药师', '门诊发药', 1)");
        jdbcTemplate.update("insert into doctor_schedule (schedule_id, doctor_id, dept_id, work_date, time_period, start_time, end_time, total_quota, remain_quota, registration_fee, status) values (1, 2, 2, '2026-06-22', '上午', '08:00:00', '12:00:00', 40, 38, 15.00, '可预约')");
    }
}
