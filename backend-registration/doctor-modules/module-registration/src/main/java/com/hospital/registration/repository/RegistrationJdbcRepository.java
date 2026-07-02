package com.hospital.registration.repository;

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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Repository
public class RegistrationJdbcRepository {
    private static final String REGISTRATION_FEE = "挂号费";
    private static final String REGISTRATION_ITEM_TYPE = "挂号";
    private static final String PENDING_PAYMENT = "待支付";
    private static final String PAID = "已支付";
    private static final String WAITING = "待接诊";
    private static final String CALLED = "接诊中";
    private static final String REFUNDED = "已退费";
    private static final String REFUNDED_QUEUE = "已取消";
    private static final String OFFLINE = "线下";
    private static final String ONLINE = "线上";
    private static final String AVAILABLE = "可预约";
    private static final String PAYMENT_SUCCESS = "成功";
    private static final String REFUND_COMPLETE = "已完成";
    private static final String VISIT_WAITING = "待接诊";
    private static final String REGISTRATION_VIEW_SELECT = """
            select r.registration_id,
                   r.registration_no,
                   r.patient_id,
                   p.patient_name,
                   r.doctor_id,
                   u.real_name as doctor_name,
                   dep.dept_name,
                   r.schedule_id,
                   s.work_date,
                   s.time_period,
                   r.queue_no,
                   r.fee_status,
                   r.status
              from registration r
              left join patient p on p.patient_id = r.patient_id
              left join doctor d on d.doctor_id = r.doctor_id
              left join sys_user u on u.user_id = d.user_id
              left join department dep on dep.dept_id = r.dept_id
              left join doctor_schedule s on s.schedule_id = r.schedule_id
            """;
    private static final String FEE_ORDER_VIEW_SELECT = """
            select fo.fee_order_id,
                   fo.business_id,
                   fo.patient_id,
                   p.patient_name,
                   fo.registration_id,
                   fo.business_type,
                   fo.total_amount,
                   fo.paid_amount,
                   fo.refund_amount,
                   fo.status,
                   r.status as business_status
              from fee_order fo
              left join patient p on p.patient_id = fo.patient_id
              left join registration r on r.registration_id = fo.registration_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public RegistrationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public PatientView syncPatient(PatientSyncRequest request) {
        if (request == null) {
            throw new BusinessException("患者信息不能为空");
        }
        String patientName = trimToEmpty(request.patientName());
        String phone = trimToEmpty(request.phone());
        String idCard = trimToEmpty(request.idCard());
        if (patientName.isBlank()) {
            throw new BusinessException("患者姓名不能为空");
        }
        if (idCard.isBlank() && phone.isBlank()) {
            throw new BusinessException("患者身份证号或手机号至少填写一项");
        }

        PatientView existing = findPatientByIdentity(patientName, idCard, phone);
        if (existing != null) {
            jdbcTemplate.update("""
                            update patient
                               set patient_name = ?,
                                   gender = ?,
                                   phone = ?,
                                   allergy_history = ?,
                                   past_history = ?
                             where patient_id = ?
                            """,
                    patientName,
                    trimToEmpty(request.gender()),
                    phone,
                    trimToEmpty(request.allergyHistory()),
                    trimToEmpty(request.pastHistory()),
                    existing.patientId()
            );
            return findPatient(existing.patientId());
        }

        String patientNo = nextBusinessNo("P", "patient", "patient_id", 3);
        long patientId = insertAndReturnKey("""
                        insert into patient (
                            patient_no, patient_name, gender, id_card, phone,
                            allergy_history, past_history, status
                        ) values (?, ?, ?, ?, ?, ?, ?, 1)
                        """,
                patientNo,
                patientName,
                trimToEmpty(request.gender()),
                emptyToNull(idCard),
                phone,
                trimToEmpty(request.allergyHistory()),
                trimToEmpty(request.pastHistory())
        );
        return findPatient(patientId);
    }

    public PatientView findPatient(Long patientId) {
        PatientView patient = queryOne("""
                        select patient_id, patient_no, patient_name, gender, id_card, phone,
                               allergy_history, past_history
                          from patient
                         where patient_id = ?
                        """,
                (rs, rowNum) -> new PatientView(
                        rs.getLong("patient_id"),
                        rs.getString("patient_no"),
                        rs.getString("patient_name"),
                        rs.getString("gender"),
                        rs.getString("id_card"),
                        rs.getString("phone"),
                        rs.getString("allergy_history"),
                        rs.getString("past_history")
                ),
                patientId
        );
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return patient;
    }

    public List<DepartmentView> listDepartments() {
        return jdbcTemplate.query("""
                        select dept_id, dept_code, dept_name, dept_type, location, description
                          from department
                         where status = 1
                         order by dept_id
                        """,
                (rs, rowNum) -> new DepartmentView(
                        rs.getLong("dept_id"),
                        rs.getString("dept_code"),
                        rs.getString("dept_name"),
                        rs.getString("dept_type"),
                        rs.getString("location"),
                        rs.getString("description")
                ));
    }

    public List<DoctorView> listDoctors(Long deptId) {
        String sql = """
                select d.doctor_id,
                       coalesce(u.real_name, d.doctor_no) as doctor_name,
                       d.dept_id,
                       dep.dept_name,
                       d.title,
                       d.specialty
                  from doctor d
                  left join sys_user u on u.user_id = d.user_id
                  left join department dep on dep.dept_id = d.dept_id
                 where d.status = 1
                """;
        Object[] args = deptId == null ? new Object[]{} : new Object[]{deptId};
        if (deptId != null) {
            sql += " and d.dept_id = ?";
        }
        sql += " order by d.doctor_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DoctorView(
                rs.getLong("doctor_id"),
                rs.getString("doctor_name"),
                rs.getLong("dept_id"),
                rs.getString("dept_name"),
                rs.getString("title"),
                rs.getString("specialty")
        ), args);
    }

    public List<DoctorScheduleView> listSchedules(Long deptId, Long doctorId, LocalDate workDate) {
        StringBuilder sql = new StringBuilder("""
                select s.schedule_id,
                       s.doctor_id,
                       coalesce(u.real_name, d.doctor_no) as doctor_name,
                       s.dept_id,
                       dep.dept_name,
                       s.work_date,
                       s.time_period,
                       s.start_time,
                       s.end_time,
                       s.total_quota,
                       s.remain_quota,
                       s.registration_fee,
                       s.status
                  from doctor_schedule s
                  left join doctor d on d.doctor_id = s.doctor_id
                  left join sys_user u on u.user_id = d.user_id
                  left join department dep on dep.dept_id = s.dept_id
                 where s.status = ?
                   and s.remain_quota > 0
                """);
        List<Object> args = new java.util.ArrayList<>();
        args.add(AVAILABLE);
        if (deptId != null) {
            sql.append(" and s.dept_id = ?");
            args.add(deptId);
        }
        if (doctorId != null) {
            sql.append(" and s.doctor_id = ?");
            args.add(doctorId);
        }
        if (workDate != null) {
            sql.append(" and s.work_date = ?");
            args.add(Date.valueOf(workDate));
        }
        sql.append(" order by s.work_date, s.start_time, s.schedule_id");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new DoctorScheduleView(
                rs.getLong("schedule_id"),
                rs.getLong("doctor_id"),
                rs.getString("doctor_name"),
                rs.getLong("dept_id"),
                rs.getString("dept_name"),
                rs.getObject("work_date", LocalDate.class),
                rs.getString("time_period"),
                rs.getObject("start_time", LocalTime.class),
                rs.getObject("end_time", LocalTime.class),
                rs.getInt("total_quota"),
                rs.getInt("remain_quota"),
                rs.getBigDecimal("registration_fee"),
                rs.getString("status")
        ), args.toArray());
    }

    @Transactional
    public RegistrationView createOfflineRegistration(Long patientId, Long doctorId, Long scheduleId) {
        findPatient(patientId);
        ScheduleSnapshot schedule = requireSchedule(scheduleId);
        if (!Objects.equals(schedule.doctorId(), doctorId)) {
            throw new BusinessException("医生与排班不匹配");
        }
        int updated = jdbcTemplate.update("""
                        update doctor_schedule
                           set remain_quota = remain_quota - 1
                         where schedule_id = ?
                           and remain_quota > 0
                        """,
                scheduleId
        );
        if (updated == 0) {
            throw new BusinessException("当前排班暂无剩余号源");
        }

        String registrationNo = nextBusinessNo("REG", "registration", "registration_id", 6);
        long registrationId = insertAndReturnKey("""
                        insert into registration (
                            patient_id, dept_id, doctor_id, schedule_id, source,
                            registration_no, registration_fee, fee_status, status, registered_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                        """,
                patientId,
                schedule.deptId(),
                doctorId,
                scheduleId,
                OFFLINE,
                registrationNo,
                schedule.registrationFee(),
                PENDING_PAYMENT,
                PENDING_PAYMENT
        );

        long feeOrderId = insertAndReturnKey("""
                        insert into fee_order (
                            order_no, patient_id, registration_id, business_type, business_id,
                            total_amount, paid_amount, refund_amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                nextBusinessNo("FEE", "fee_order", "fee_order_id", 6),
                patientId,
                registrationId,
                "REGISTRATION",
                registrationId,
                schedule.registrationFee(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PENDING_PAYMENT
        );
        jdbcTemplate.update("""
                        insert into fee_order_item (
                            fee_order_id, item_type, item_id, item_name,
                            unit_price, quantity, amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                feeOrderId,
                REGISTRATION_ITEM_TYPE,
                registrationId,
                REGISTRATION_FEE,
                schedule.registrationFee(),
                1,
                schedule.registrationFee(),
                PENDING_PAYMENT
        );

        return findRegistration(registrationId);
    }

    @Transactional
    public RegistrationView createOnlineRegistration(Long patientId, Long doctorId, Long scheduleId, boolean paid) {
        findPatient(patientId);
        ScheduleSnapshot schedule = requireSchedule(scheduleId);
        if (!Objects.equals(schedule.doctorId(), doctorId)) {
            throw new BusinessException("医生与排班不匹配");
        }
        int updated = jdbcTemplate.update("""
                        update doctor_schedule
                           set remain_quota = remain_quota - 1
                         where schedule_id = ?
                           and remain_quota > 0
                        """,
                scheduleId
        );
        if (updated == 0) {
            throw new BusinessException("当前排班暂无剩余号源");
        }

        String registrationNo = nextBusinessNo("REG", "registration", "registration_id", 6);
        long registrationId = insertAndReturnKey("""
                        insert into registration (
                            patient_id, dept_id, doctor_id, schedule_id, source,
                            registration_no, registration_fee, fee_status, status, registered_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                        """,
                patientId,
                schedule.deptId(),
                doctorId,
                scheduleId,
                ONLINE,
                registrationNo,
                schedule.registrationFee(),
                paid ? PAID : PENDING_PAYMENT,
                paid ? WAITING : PENDING_PAYMENT
        );

        long feeOrderId = insertAndReturnKey("""
                        insert into fee_order (
                            order_no, patient_id, registration_id, business_type, business_id,
                            total_amount, paid_amount, refund_amount, status, paid_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                nextBusinessNo("FEE", "fee_order", "fee_order_id", 6),
                patientId,
                registrationId,
                "REGISTRATION",
                registrationId,
                schedule.registrationFee(),
                paid ? schedule.registrationFee() : BigDecimal.ZERO,
                BigDecimal.ZERO,
                paid ? PAID : PENDING_PAYMENT,
                paid ? java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()) : null
        );
        jdbcTemplate.update("""
                        insert into fee_order_item (
                            fee_order_id, item_type, item_id, item_name,
                            unit_price, quantity, amount, status
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                feeOrderId,
                REGISTRATION_ITEM_TYPE,
                registrationId,
                REGISTRATION_FEE,
                schedule.registrationFee(),
                1,
                schedule.registrationFee(),
                paid ? PAID : PENDING_PAYMENT
        );

        return findRegistration(registrationId);
    }

    public List<RegistrationView> listOnlinePending() {
        return jdbcTemplate.query(REGISTRATION_VIEW_SELECT + """
                         where r.source = ?
                           and r.queue_no is null
                           and r.status in (?, ?)
                         order by r.registered_at, r.registration_id
                        """,
                this::mapRegistration,
                ONLINE,
                PENDING_PAYMENT,
                WAITING
        );
    }

    @Transactional
    public RegistrationView confirmOnlineRegistration(Long registrationId) {
        RegistrationSnapshot registration = findRegistrationSnapshot(registrationId);
        if (!ONLINE.equals(registration.source())) {
            throw new BusinessException("不是线上挂号记录");
        }
        if (!PAID.equals(registration.feeStatus())) {
            throw new BusinessException("未支付挂号费，不能确认线上挂号");
        }

        Integer queueNo = registration.queueNo();
        if (queueNo == null) {
            queueNo = nextQueueNo(registration.doctorId(), registration.scheduleId());
        }
        jdbcTemplate.update("""
                        update registration
                           set status = ?,
                               queue_no = ?
                         where registration_id = ?
                        """,
                WAITING,
                queueNo,
                registrationId
        );
        RegistrationView confirmed = findRegistration(registrationId);
        createOutpatientVisitIfPossible(confirmed);
        return confirmed;
    }

    @Transactional
    public RegistrationView chargeRegistration(Long registrationId, String payMethod) {
        RegistrationView registration = findRegistration(registrationId);
        RegistrationSnapshot snapshot = findRegistrationSnapshot(registrationId);
        if (REFUNDED.equals(registration.feeStatus()) || REFUNDED_QUEUE.equals(registration.status())) {
            return registration;
        }
        if (PAID.equals(registration.feeStatus()) && registration.queueNo() != null) {
            return registration;
        }

        Integer queueNo = registration.queueNo();
        if (queueNo == null && OFFLINE.equals(snapshot.source())) {
            queueNo = nextQueueNo(registration.doctorId(), registration.scheduleId());
        }

        jdbcTemplate.update("""
                        update registration
                           set fee_status = ?,
                               status = ?,
                               queue_no = ?
                         where registration_id = ?
                        """,
                PAID,
                WAITING,
                queueNo,
                registrationId
        );
        jdbcTemplate.update("""
                        update fee_order
                           set status = ?,
                               paid_amount = total_amount,
                               paid_at = current_timestamp
                         where business_type = ?
                           and business_id = ?
                        """,
                PAID,
                "REGISTRATION",
                registrationId
        );
        jdbcTemplate.update("""
                        update fee_order_item
                           set status = ?
                         where item_type = ?
                           and item_id = ?
                        """,
                PAID,
                REGISTRATION_ITEM_TYPE,
                registrationId
        );
        insertPaymentRecordIfPossible(registrationId, payMethod);
        RegistrationView charged = findRegistration(registrationId);
        createOutpatientVisitIfPossible(charged);
        return charged;
    }

    @Transactional
    public FeeOrderView chargeFeeOrder(Long feeOrderId, String payMethod) {
        FeeOrderChargeSnapshot feeOrder = findFeeOrderChargeSnapshot(feeOrderId);
        if (feeOrder == null) {
            throw new BusinessException("收费单不存在");
        }
        if (REFUNDED.equals(feeOrder.status())) {
            throw new BusinessException("收费单已退费，不能重复收费");
        }

        jdbcTemplate.update("""
                        update fee_order
                           set status = ?,
                               paid_amount = total_amount,
                               paid_at = current_timestamp
                         where fee_order_id = ?
                        """,
                PAID,
                feeOrderId
        );
        jdbcTemplate.update("""
                        update fee_order_item
                           set status = ?
                         where fee_order_id = ?
                        """,
                PAID,
                feeOrderId
        );

        synchronizeBusinessAfterCharge(feeOrder);
        insertPaymentRecordForFeeOrderIfPossible(feeOrderId, payMethod, "窗口收费");
        return findFeeOrderView(feeOrderId);
    }

    public List<RegistrationView> listQueue(Long doctorId, Long scheduleId) {
        return listQueue(doctorId, scheduleId, null);
    }

    public List<RegistrationView> listQueue(Long doctorId, Long scheduleId, LocalDate workDate) {
        StringBuilder sql = new StringBuilder(REGISTRATION_VIEW_SELECT + """
                 where r.fee_status = ?
                   and r.status = ?
                """);
        List<Object> args = new java.util.ArrayList<>();
        args.add(PAID);
        args.add(WAITING);
        if (doctorId != null) {
            sql.append(" and r.doctor_id = ?");
            args.add(doctorId);
        }
        if (scheduleId != null) {
            sql.append(" and r.schedule_id = ?");
            args.add(scheduleId);
        }
        if (workDate != null) {
            sql.append(" and s.work_date = ?");
            args.add(Date.valueOf(workDate));
        }
        sql.append(" order by r.queue_no, r.registration_id");
        return jdbcTemplate.query(sql.toString(), this::mapRegistration, args.toArray());
    }

    @Transactional
    public RegistrationView callNext() {
        return callNext(null, null);
    }

    @Transactional
    public RegistrationView callNext(Long doctorId, Long scheduleId) {
        return callNext(doctorId, scheduleId, null);
    }

    @Transactional
    public RegistrationView callNext(Long doctorId, Long scheduleId, LocalDate workDate) {
        StringBuilder sql = new StringBuilder(REGISTRATION_VIEW_SELECT + """
                         where r.fee_status = ?
                           and r.status = ?
                        """);
        List<Object> args = new java.util.ArrayList<>();
        args.add(PAID);
        args.add(WAITING);
        if (doctorId != null) {
            sql.append(" and r.doctor_id = ?");
            args.add(doctorId);
        }
        if (scheduleId != null) {
            sql.append(" and r.schedule_id = ?");
            args.add(scheduleId);
        }
        if (workDate != null) {
            sql.append(" and s.work_date = ?");
            args.add(Date.valueOf(workDate));
        }
        sql.append(" order by r.queue_no, r.registration_id limit 1");

        RegistrationView next = queryOne(sql.toString(),
                this::mapRegistration,
                args.toArray()
        );
        if (next == null) {
            throw new BusinessException("挂号记录未进入待接诊队列");
        }
        if (hasColumn("registration", "called_at")) {
            jdbcTemplate.update(
                    "update registration set status = ?, called_at = current_timestamp where registration_id = ?",
                    CALLED,
                    next.registrationId()
            );
        } else {
            jdbcTemplate.update("update registration set status = ? where registration_id = ?", CALLED, next.registrationId());
        }
        RegistrationView called = findRegistration(next.registrationId());
        createOutpatientVisitIfPossible(called);
        return called;
    }

    public List<FeeOrderView> listPendingFees(Long patientId, Long registrationId) {
        StringBuilder sql = new StringBuilder(FEE_ORDER_VIEW_SELECT + """
                 where fo.status = ?
                """);
        List<Object> args = new java.util.ArrayList<>();
        args.add(PENDING_PAYMENT);
        if (patientId != null) {
            sql.append(" and fo.patient_id = ?");
            args.add(patientId);
        }
        if (registrationId != null) {
            sql.append(" and fo.registration_id = ?");
            args.add(registrationId);
        }
        sql.append(" order by fo.fee_order_id");
        return jdbcTemplate.query(sql.toString(), this::mapFeeOrder, args.toArray());
    }

    public FeeHistoryView feeHistory(Long patientId, Long registrationId) {
        StringBuilder sql = new StringBuilder(FEE_ORDER_VIEW_SELECT + """
                 where 1 = 1
                """);
        List<Object> args = new java.util.ArrayList<>();
        if (patientId != null) {
            sql.append(" and fo.patient_id = ?");
            args.add(patientId);
        }
        if (registrationId != null) {
            sql.append(" and fo.registration_id = ?");
            args.add(registrationId);
        }
        sql.append(" order by fo.fee_order_id");
        List<FeeOrderView> orders = jdbcTemplate.query(sql.toString(), this::mapFeeOrder, args.toArray());
        return new FeeHistoryView(patientId, registrationId, orders);
    }

    public RefundCheckView checkRefund(Long feeOrderId) {
        FeeOrderRefundSnapshot feeOrder = queryOne("""
                        select fo.fee_order_id, fo.business_id, fo.business_type, fo.status, r.status as registration_status
                          from fee_order fo
                          left join registration r on r.registration_id = fo.business_id
                         where fo.fee_order_id = ?
                        """,
                (rs, rowNum) -> new FeeOrderRefundSnapshot(
                        rs.getLong("fee_order_id"),
                        rs.getLong("business_id"),
                        rs.getString("business_type"),
                        rs.getString("status"),
                        rs.getString("registration_status")
                ),
                feeOrderId
        );
        if (feeOrder == null) {
            throw new BusinessException("收费单不存在");
        }
        if (REFUNDED.equals(feeOrder.status())) {
            return new RefundCheckView(feeOrderId, false, "收费单已退费");
        }
        if ("REGISTRATION".equals(feeOrder.businessType())) {
            if (REFUNDED_QUEUE.equals(feeOrder.registrationStatus())) {
                return new RefundCheckView(feeOrderId, false, "挂号记录已取消");
            }
            if (CALLED.equals(feeOrder.registrationStatus()) || "已完成".equals(feeOrder.registrationStatus()) || "爽约".equals(feeOrder.registrationStatus())) {
                return new RefundCheckView(feeOrderId, false, "已接诊患者不允许退号");
            }
        }
        if (isExamLabBusiness(feeOrder.businessType()) && hasExecutedExamLabItems(feeOrder.businessId())) {
            return new RefundCheckView(feeOrderId, false, "检查/检验项目已执行，不允许退费");
        }
        if ("PRESCRIPTION".equals(feeOrder.businessType()) && hasDispensedPrescription(feeOrder.businessId())) {
            return new RefundCheckView(feeOrderId, false, "处方药品已发药，不允许退费");
        }
        return new RefundCheckView(feeOrderId, true, "可退费");
    }

    @Transactional
    public FeeOrderView refund(Long feeOrderId, String reason) {
        RefundCheckView check = checkRefund(feeOrderId);
        if (!check.refundable()) {
            throw new BusinessException(check.reason());
        }
        FeeOrderRefundSnapshot feeOrder = queryOne("""
                        select fee_order_id, business_id, business_type, status, null as registration_status
                          from fee_order
                         where fee_order_id = ?
                        """,
                (rs, rowNum) -> new FeeOrderRefundSnapshot(
                        rs.getLong("fee_order_id"),
                        rs.getLong("business_id"),
                        rs.getString("business_type"),
                        rs.getString("status"),
                        rs.getString("registration_status")
                ),
                feeOrderId
        );
        if (feeOrder == null) {
            throw new BusinessException("收费单不存在");
        }
        jdbcTemplate.update("update fee_order set status = ?, refund_amount = total_amount where fee_order_id = ?", REFUNDED, feeOrderId);
        jdbcTemplate.update("update fee_order_item set status = ? where fee_order_id = ?", REFUNDED, feeOrderId);
        if ("REGISTRATION".equals(feeOrder.businessType())) {
            RegistrationView registration = findRegistration(feeOrder.businessId());
            jdbcTemplate.update("""
                            update registration
                               set fee_status = ?,
                                   status = ?
                             where registration_id = ?
                            """,
                    REFUNDED,
                    REFUNDED_QUEUE,
                    registration.registrationId()
            );
            jdbcTemplate.update("""
                            update doctor_schedule
                               set remain_quota = least(total_quota, remain_quota + 1)
                             where schedule_id = ?
                            """,
                    registration.scheduleId()
            );
            deleteWaitingOutpatientVisitIfPossible(registration.registrationId());
        }
        insertRefundRecordIfPossible(feeOrderId, feeOrder.businessType(), reason);
        return findFeeOrderView(feeOrderId);
    }

    private PatientView findPatientByIdentity(String patientName, String idCard, String phone) {
        if (!idCard.isBlank()) {
            PatientView byIdCard = queryOne("""
                            select patient_id, patient_no, patient_name, gender, id_card, phone,
                                   allergy_history, past_history
                              from patient
                             where id_card = ?
                            """,
                    (rs, rowNum) -> new PatientView(
                            rs.getLong("patient_id"),
                            rs.getString("patient_no"),
                            rs.getString("patient_name"),
                            rs.getString("gender"),
                            rs.getString("id_card"),
                            rs.getString("phone"),
                            rs.getString("allergy_history"),
                            rs.getString("past_history")
                    ),
                    idCard
            );
            if (byIdCard != null) {
                return byIdCard;
            }
        }
        if (!patientName.isBlank() && !phone.isBlank()) {
            return queryOne("""
                            select patient_id, patient_no, patient_name, gender, id_card, phone,
                                   allergy_history, past_history
                              from patient
                             where patient_name = ?
                               and phone = ?
                            """,
                    (rs, rowNum) -> new PatientView(
                            rs.getLong("patient_id"),
                            rs.getString("patient_no"),
                            rs.getString("patient_name"),
                            rs.getString("gender"),
                            rs.getString("id_card"),
                            rs.getString("phone"),
                            rs.getString("allergy_history"),
                            rs.getString("past_history")
                    ),
                    patientName,
                    phone
            );
        }
        return null;
    }

    private RegistrationView findRegistration(Long registrationId) {
        RegistrationView registration = queryOne(REGISTRATION_VIEW_SELECT + """
                         where r.registration_id = ?
                        """,
                this::mapRegistration,
                registrationId
        );
        if (registration == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return registration;
    }

    private FeeOrderView findFeeOrderView(Long feeOrderId) {
        FeeOrderView feeOrder = queryOne(FEE_ORDER_VIEW_SELECT + """
                         where fo.fee_order_id = ?
                        """,
                this::mapFeeOrder,
                feeOrderId
        );
        if (feeOrder == null) {
            throw new BusinessException("收费单不存在");
        }
        return feeOrder;
    }

    private ScheduleSnapshot requireSchedule(Long scheduleId) {
        ScheduleSnapshot schedule = queryOne("""
                        select schedule_id, doctor_id, dept_id, remain_quota, total_quota, registration_fee
                          from doctor_schedule
                         where schedule_id = ?
                        """,
                (rs, rowNum) -> new ScheduleSnapshot(
                        rs.getLong("schedule_id"),
                        rs.getLong("doctor_id"),
                        rs.getLong("dept_id"),
                        rs.getInt("remain_quota"),
                        rs.getInt("total_quota"),
                        rs.getBigDecimal("registration_fee")
                ),
                scheduleId
        );
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        if (schedule.remainQuota() <= 0) {
            throw new BusinessException("当前排班暂无剩余号源");
        }
        return schedule;
    }

    private RegistrationSnapshot findRegistrationSnapshot(Long registrationId) {
        RegistrationSnapshot registration = queryOne("""
                        select registration_id, registration_no, patient_id, dept_id, doctor_id, schedule_id,
                               source, queue_no, fee_status, status
                          from registration
                         where registration_id = ?
                        """,
                (rs, rowNum) -> {
                    int queueNo = rs.getInt("queue_no");
                    Integer queueNoValue = rs.wasNull() ? null : queueNo;
                    return new RegistrationSnapshot(
                            rs.getLong("registration_id"),
                            rs.getString("registration_no"),
                            rs.getLong("patient_id"),
                            rs.getLong("dept_id"),
                            rs.getLong("doctor_id"),
                            rs.getLong("schedule_id"),
                            rs.getString("source"),
                            queueNoValue,
                            rs.getString("fee_status"),
                            rs.getString("status")
                    );
                },
                registrationId
        );
        if (registration == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return registration;
    }

    private FeeOrderChargeSnapshot findFeeOrderChargeSnapshot(Long feeOrderId) {
        return queryOne("""
                        select fee_order_id, patient_id, registration_id, business_type,
                               business_id, total_amount, status
                          from fee_order
                         where fee_order_id = ?
                        """,
                (rs, rowNum) -> {
                    long registrationId = rs.getLong("registration_id");
                    Long registrationIdValue = rs.wasNull() ? null : registrationId;
                    return new FeeOrderChargeSnapshot(
                            rs.getLong("fee_order_id"),
                            rs.getLong("patient_id"),
                            registrationIdValue,
                            rs.getString("business_type"),
                            rs.getLong("business_id"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("status")
                    );
                },
                feeOrderId
        );
    }

    private int nextQueueNo(Long doctorId, Long scheduleId) {
        Integer next = jdbcTemplate.queryForObject("""
                        select coalesce(max(queue_no), 0) + 1
                          from registration
                         where doctor_id = ?
                           and schedule_id = ?
                           and fee_status = ?
                        """,
                Integer.class,
                doctorId,
                scheduleId,
                PAID
        );
        return next == null ? 1 : next;
    }

    private void insertPaymentRecordIfPossible(Long registrationId, String payMethod) {
        FeePaymentSnapshot fee = queryOne("""
                        select fo.fee_order_id, fo.total_amount, p.patient_name
                          from fee_order fo
                          left join patient p on p.patient_id = fo.patient_id
                         where fo.business_type = ?
                           and fo.business_id = ?
                         order by fo.fee_order_id
                         limit 1
                        """,
                (rs, rowNum) -> new FeePaymentSnapshot(
                        rs.getLong("fee_order_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("patient_name")
                ),
                "REGISTRATION",
                registrationId
        );
        if (fee != null) {
            insertPaymentRecordForFeeOrderIfPossible(fee.feeOrderId(), payMethod, "挂号收费");
        }
    }

    private void insertPaymentRecordForFeeOrderIfPossible(Long feeOrderId, String payMethod, String remark) {
        if (!hasTable("payment_record")) {
            return;
        }
        FeePaymentSnapshot fee = queryOne("""
                        select fo.fee_order_id, fo.total_amount, p.patient_name
                          from fee_order fo
                          left join patient p on p.patient_id = fo.patient_id
                         where fo.fee_order_id = ?
                        """,
                (rs, rowNum) -> new FeePaymentSnapshot(
                        rs.getLong("fee_order_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("patient_name")
                ),
                feeOrderId
        );
        if (fee == null) {
            return;
        }
        Integer exists = jdbcTemplate.queryForObject(
                "select count(*) from payment_record where fee_order_id = ? and status = ?",
                Integer.class,
                fee.feeOrderId(),
                PAYMENT_SUCCESS
        );
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.update("""
                        insert into payment_record (
                            fee_order_id, payment_no, payment_method, payment_amount,
                            payer_name, status, paid_at, remark
                        ) values (?, ?, ?, ?, ?, ?, current_timestamp, ?)
                        """,
                fee.feeOrderId(),
                nextBusinessNo("PAY", "payment_record", "payment_id", 6),
                trimToEmpty(payMethod).isBlank() ? "现金" : trimToEmpty(payMethod),
                fee.totalAmount(),
                fee.patientName(),
                PAYMENT_SUCCESS,
                trimToEmpty(remark).isBlank() ? "窗口收费" : trimToEmpty(remark)
        );
    }

    private void synchronizeBusinessAfterCharge(FeeOrderChargeSnapshot feeOrder) {
        if ("REGISTRATION".equals(feeOrder.businessType())) {
            synchronizeRegistrationAfterCharge(feeOrder);
            return;
        }
        if (isExamLabBusiness(feeOrder.businessType())) {
            synchronizeExamLabAfterCharge(feeOrder.businessId());
            return;
        }
        if ("PRESCRIPTION".equals(feeOrder.businessType())) {
            synchronizePrescriptionAfterCharge(feeOrder.businessId());
        }
    }

    private void synchronizeRegistrationAfterCharge(FeeOrderChargeSnapshot feeOrder) {
        Long registrationId = feeOrder.registrationId() == null ? feeOrder.businessId() : feeOrder.registrationId();
        if (registrationId == null) {
            return;
        }
        RegistrationView registration = findRegistration(registrationId);
        RegistrationSnapshot snapshot = findRegistrationSnapshot(registrationId);
        if (REFUNDED.equals(registration.feeStatus()) || REFUNDED_QUEUE.equals(registration.status())) {
            return;
        }

        Integer queueNo = registration.queueNo();
        if (queueNo == null && OFFLINE.equals(snapshot.source())) {
            queueNo = nextQueueNo(registration.doctorId(), registration.scheduleId());
        }
        jdbcTemplate.update("""
                        update registration
                           set fee_status = ?,
                               status = ?,
                               queue_no = ?
                         where registration_id = ?
                        """,
                PAID,
                WAITING,
                queueNo,
                registrationId
        );
        createOutpatientVisitIfPossible(findRegistration(registrationId));
    }

    private void synchronizeExamLabAfterCharge(Long orderId) {
        if (!hasTable("exam_lab_order")) {
            return;
        }
        if (hasColumn("exam_lab_order", "fee_status") && hasColumn("exam_lab_order", "status")) {
            jdbcTemplate.update("""
                            update exam_lab_order
                               set fee_status = ?,
                                   status = ?
                             where order_id = ?
                            """,
                    PAID,
                    "待执行",
                    orderId
            );
        } else if (hasColumn("exam_lab_order", "fee_status")) {
            jdbcTemplate.update("update exam_lab_order set fee_status = ? where order_id = ?", PAID, orderId);
        } else if (hasColumn("exam_lab_order", "status")) {
            jdbcTemplate.update("update exam_lab_order set status = ? where order_id = ?", "待执行", orderId);
        }
        if (hasTable("exam_lab_order_item") && hasColumn("exam_lab_order_item", "status")) {
            jdbcTemplate.update("update exam_lab_order_item set status = ? where order_id = ?", "待执行", orderId);
        }
    }

    private void synchronizePrescriptionAfterCharge(Long prescriptionId) {
        if (!hasTable("prescription")) {
            return;
        }
        if (hasColumn("prescription", "fee_status") && hasColumn("prescription", "status")) {
            jdbcTemplate.update("""
                            update prescription
                               set fee_status = ?,
                                   status = ?
                             where prescription_id = ?
                            """,
                    PAID,
                    "待发药",
                    prescriptionId
            );
        } else if (hasColumn("prescription", "fee_status")) {
            jdbcTemplate.update("update prescription set fee_status = ? where prescription_id = ?", PAID, prescriptionId);
        } else if (hasColumn("prescription", "status")) {
            jdbcTemplate.update("update prescription set status = ? where prescription_id = ?", "待发药", prescriptionId);
        }
        if (hasTable("prescription_item") && hasColumn("prescription_item", "status")) {
            jdbcTemplate.update("update prescription_item set status = ? where prescription_id = ?", "待发药", prescriptionId);
        }
        if (hasTable("pharmacy_dispense") && hasColumn("pharmacy_dispense", "status")) {
            jdbcTemplate.update("""
                            update pharmacy_dispense
                               set status = ?
                             where prescription_id = ?
                               and status in ('待缴费', '待支付')
                            """,
                    "待发药",
                    prescriptionId
            );
        }
        createPharmacyDispenseIfPossible(prescriptionId);
    }

    private void createPharmacyDispenseIfPossible(Long prescriptionId) {
        if (!hasTable("pharmacy_dispense") || !hasTable("prescription")) {
            return;
        }
        Integer exists = jdbcTemplate.queryForObject(
                "select count(*) from pharmacy_dispense where prescription_id = ?",
                Integer.class,
                prescriptionId
        );
        if (exists != null && exists > 0) {
            return;
        }
        PrescriptionDispenseSnapshot prescription = queryOne("""
                        select prescription_id, patient_id, total_amount
                          from prescription
                         where prescription_id = ?
                        """,
                (rs, rowNum) -> new PrescriptionDispenseSnapshot(
                        rs.getLong("prescription_id"),
                        rs.getLong("patient_id"),
                        rs.getBigDecimal("total_amount")
                ),
                prescriptionId
        );
        if (prescription == null) {
            return;
        }
        Long pharmacyDoctorId = findDefaultPharmacyDoctorId();
        if (pharmacyDoctorId == null) {
            return;
        }
        jdbcTemplate.update("""
                        insert into pharmacy_dispense (
                            prescription_id, patient_id, pharmacy_doctor_id,
                            dispense_no, total_amount, status
                        ) values (?, ?, ?, ?, ?, ?)
                        """,
                prescription.prescriptionId(),
                prescription.patientId(),
                pharmacyDoctorId,
                nextBusinessNo("DISP", "pharmacy_dispense", "dispense_id", 6),
                prescription.totalAmount() == null ? BigDecimal.ZERO : prescription.totalAmount(),
                "待发药"
        );
    }

    private Long findDefaultPharmacyDoctorId() {
        if (!hasTable("doctor") || !hasTable("department")) {
            return null;
        }
        return queryOne("""
                        select d.doctor_id
                          from doctor d
                          join department dep on dep.dept_id = d.dept_id
                         where d.status = 1
                           and dep.status = 1
                           and dep.dept_type = 'PHARMACY'
                         order by d.doctor_id
                         limit 1
                        """,
                (rs, rowNum) -> rs.getLong("doctor_id")
        );
    }

    private void insertRefundRecordIfPossible(Long feeOrderId, String businessType, String reason) {
        if (!hasTable("refund_record")) {
            return;
        }
        RefundPaymentSnapshot payment = queryOne("""
                        select fo.total_amount, pr.payment_id
                          from fee_order fo
                          left join payment_record pr on pr.fee_order_id = fo.fee_order_id
                         where fo.fee_order_id = ?
                         order by pr.payment_id desc
                         limit 1
                        """,
                (rs, rowNum) -> {
                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                    long paymentId = rs.getLong("payment_id");
                    Long paymentIdValue = rs.wasNull() ? null : paymentId;
                    return new RefundPaymentSnapshot(
                            totalAmount,
                            paymentIdValue
                    );
                },
                feeOrderId
        );
        if (payment == null) {
            return;
        }
        Integer exists = jdbcTemplate.queryForObject(
                "select count(*) from refund_record where fee_order_id = ? and status = ?",
                Integer.class,
                feeOrderId,
                REFUND_COMPLETE
        );
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.update("""
                        insert into refund_record (
                            fee_order_id, payment_id, refund_no, refund_type,
                            refund_amount, reason, status, requested_at, completed_at
                        ) values (?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp)
                        """,
                feeOrderId,
                payment.paymentId(),
                nextBusinessNo("REF", "refund_record", "refund_id", 6),
                refundType(businessType),
                payment.totalAmount(),
                trimToEmpty(reason),
                REFUND_COMPLETE
        );
    }

    private void createOutpatientVisitIfPossible(RegistrationView registration) {
        if (!hasTable("outpatient_visit")) {
            return;
        }
        RegistrationSnapshot snapshot = findRegistrationSnapshot(registration.registrationId());
        Integer exists = jdbcTemplate.queryForObject(
                "select count(*) from outpatient_visit where registration_id = ?",
                Integer.class,
                registration.registrationId()
        );
        if (exists != null && exists > 0) {
            return;
        }
        jdbcTemplate.update("""
                        insert into outpatient_visit (
                            registration_id, patient_id, doctor_id, dept_id,
                            visit_no, queue_no, status, started_at
                        ) values (?, ?, ?, ?, ?, ?, ?, current_timestamp)
                        """,
                snapshot.registrationId(),
                snapshot.patientId(),
                snapshot.doctorId(),
                snapshot.deptId(),
                nextBusinessNo("VIS", "outpatient_visit", "visit_id", 6),
                snapshot.queueNo(),
                VISIT_WAITING
        );
    }

    private void deleteWaitingOutpatientVisitIfPossible(Long registrationId) {
        if (!hasTable("outpatient_visit")) {
            return;
        }
        jdbcTemplate.update("""
                        delete from outpatient_visit
                         where registration_id = ?
                           and status = ?
                        """,
                registrationId,
                VISIT_WAITING
        );
    }

    private boolean hasExecutedExamLabItems(Long orderId) {
        if (!hasTable("exam_lab_order_item")) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                        select count(*)
                          from exam_lab_order_item
                         where order_id = ?
                           and (executed_at is not null or status in ('已执行', '已完成', '已发布'))
                        """,
                Integer.class,
                orderId
        );
        return count != null && count > 0;
    }

    private boolean hasDispensedPrescription(Long prescriptionId) {
        if (!hasTable("pharmacy_dispense")) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                        select count(*)
                          from pharmacy_dispense
                         where prescription_id = ?
                           and (dispensed_at is not null or status in ('已发药', '已完成'))
                        """,
                Integer.class,
                prescriptionId
        );
        return count != null && count > 0;
    }

    private boolean hasTable(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            String normalized = tableName.toLowerCase();
            try (java.sql.ResultSet tables = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"})) {
                while (tables.next()) {
                    if (normalized.equals(tables.getString("TABLE_NAME").toLowerCase())) {
                        return true;
                    }
                }
            }
            return false;
        }));
    }

    private boolean hasColumn(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            String normalizedTable = tableName.toLowerCase();
            String normalizedColumn = columnName.toLowerCase();
            try (java.sql.ResultSet columns = connection.getMetaData().getColumns(null, null, null, null)) {
                while (columns.next()) {
                    if (normalizedTable.equals(columns.getString("TABLE_NAME").toLowerCase())
                            && normalizedColumn.equals(columns.getString("COLUMN_NAME").toLowerCase())) {
                        return true;
                    }
                }
            }
            return false;
        }));
    }

    private RegistrationView mapRegistration(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        int queueNo = rs.getInt("queue_no");
        Integer queueNoValue = rs.wasNull() ? null : queueNo;
        long scheduleId = rs.getLong("schedule_id");
        Long scheduleIdValue = rs.wasNull() ? null : scheduleId;
        return new RegistrationView(
                rs.getLong("registration_id"),
                rs.getString("registration_no"),
                rs.getLong("patient_id"),
                rs.getString("patient_name"),
                rs.getLong("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("dept_name"),
                scheduleIdValue,
                rs.getObject("work_date", LocalDate.class),
                rs.getString("time_period"),
                queueNoValue,
                rs.getString("fee_status"),
                rs.getString("status")
        );
    }

    private FeeOrderView mapFeeOrder(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        long registrationId = rs.getLong("registration_id");
        Long registrationIdValue = rs.wasNull() ? null : registrationId;
        String businessType = rs.getString("business_type");
        return new FeeOrderView(
                rs.getLong("fee_order_id"),
                rs.getLong("business_id"),
                rs.getLong("patient_id"),
                rs.getString("patient_name"),
                registrationIdValue,
                feeType(businessType),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("paid_amount"),
                rs.getBigDecimal("refund_amount"),
                rs.getString("status"),
                feeExecuted(businessType, rs.getLong("business_id")),
                rs.getString("business_status")
        );
    }

    private boolean feeExecuted(String businessType, Long businessId) {
        if (isExamLabBusiness(businessType)) {
            return hasExecutedExamLabItems(businessId);
        }
        if ("PRESCRIPTION".equals(businessType)) {
            return hasDispensedPrescription(businessId);
        }
        return false;
    }

    private long insertAndReturnKey(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException("新增数据失败，未返回主键");
        }
        return key.longValue();
    }

    private String nextBusinessNo(String prefix, String tableName, String idColumn, int width) {
        Long next = jdbcTemplate.queryForObject("select coalesce(max(" + idColumn + "), 0) + 1 from " + tableName, Long.class);
        long sequence = next == null ? 1L : next;
        return prefix + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%0" + width + "d", sequence);
    }

    private String feeType(String businessType) {
        if ("REGISTRATION".equals(businessType)) {
            return REGISTRATION_FEE;
        }
        if (isExamLabBusiness(businessType)) {
            return "检查检验费";
        }
        if ("PRESCRIPTION".equals(businessType)) {
            return "药品费";
        }
        return businessType == null ? "" : businessType;
    }

    private String refundType(String businessType) {
        if ("REGISTRATION".equals(businessType)) {
            return "挂号退费";
        }
        if (isExamLabBusiness(businessType)) {
            return "检查退费";
        }
        if ("PRESCRIPTION".equals(businessType)) {
            return "处方退费";
        }
        return "整单退费";
    }

    private boolean isExamLabBusiness(String businessType) {
        return "EXAM_LAB_ORDER".equals(businessType);
    }

    private <T> T queryOne(String sql, org.springframework.jdbc.core.RowMapper<T> rowMapper, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isBlank() ? null : trimmed;
    }

    private record ScheduleSnapshot(
            Long scheduleId,
            Long doctorId,
            Long deptId,
            int remainQuota,
            int totalQuota,
            BigDecimal registrationFee
    ) {
    }

    private record RegistrationSnapshot(
            Long registrationId,
            String registrationNo,
            Long patientId,
            Long deptId,
            Long doctorId,
            Long scheduleId,
            String source,
            Integer queueNo,
            String feeStatus,
            String status
    ) {
    }

    private record FeePaymentSnapshot(
            Long feeOrderId,
            BigDecimal totalAmount,
            String patientName
    ) {
    }

    private record FeeOrderChargeSnapshot(
            Long feeOrderId,
            Long patientId,
            Long registrationId,
            String businessType,
            Long businessId,
            BigDecimal totalAmount,
            String status
    ) {
    }

    private record RefundPaymentSnapshot(
            BigDecimal totalAmount,
            Long paymentId
    ) {
    }

    private record FeeOrderRefundSnapshot(
            Long feeOrderId,
            Long businessId,
            String businessType,
            String status,
            String registrationStatus
    ) {
    }

    private record PrescriptionDispenseSnapshot(
            Long prescriptionId,
            Long patientId,
            BigDecimal totalAmount
    ) {
    }
}
