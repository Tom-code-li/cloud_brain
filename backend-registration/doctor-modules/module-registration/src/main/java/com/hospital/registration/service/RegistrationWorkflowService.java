package com.hospital.registration.service;

import com.hospital.common.core.BusinessException;
import com.hospital.registration.domain.DepartmentView;
import com.hospital.registration.domain.DoctorView;
import com.hospital.registration.domain.DoctorScheduleView;
import com.hospital.registration.domain.FeeHistoryView;
import com.hospital.registration.domain.FeeOrderView;
import com.hospital.registration.domain.PatientSyncRequest;
import com.hospital.registration.domain.PatientView;
import com.hospital.registration.domain.RefundCheckView;
import com.hospital.registration.domain.RegistrationView;
import com.hospital.registration.repository.RegistrationJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RegistrationWorkflowService {
    private static final String REGISTRATION_FEE = "挂号费";
    private static final String EXAM_FEE = "检查费";
    private static final String LAB_FEE = "检验费";
    private static final String PENDING_PAYMENT = "待支付";
    private static final String PAID = "已支付";
    private static final String WAITING = "待接诊";
    private static final String CALLED = "接诊中";
    private static final String REFUNDED = "已退费";
    private static final String REFUNDED_QUEUE = "已取消";
    private static final String OFFLINE = "线下";
    private static final String ONLINE = "线上";

    private final AtomicLong patientIdGenerator = new AtomicLong(1);
    private final AtomicLong registrationIdGenerator = new AtomicLong(1);
    private final AtomicLong feeOrderIdGenerator = new AtomicLong(1);
    private final AtomicLong queueNoGenerator = new AtomicLong(1);
    private final Map<Long, PatientState> patients = new ConcurrentHashMap<>();
    private final Map<Long, RegistrationState> registrations = new ConcurrentHashMap<>();
    private final Map<Long, FeeOrderState> feeOrders = new ConcurrentHashMap<>();
    private final Map<Long, DepartmentView> departments = new LinkedHashMap<>();
    private final Map<Long, DoctorState> doctors = new LinkedHashMap<>();
    private final Map<Long, ScheduleState> schedules = new ConcurrentHashMap<>();
    private final RegistrationJdbcRepository jdbcRepository;

    public RegistrationWorkflowService() {
        this(null);
    }

    @Autowired
    public RegistrationWorkflowService(RegistrationJdbcRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
        seedCatalog();
    }

    public synchronized PatientView syncPatient(PatientSyncRequest request) {
        if (jdbcRepository != null) {
            return jdbcRepository.syncPatient(request);
        }
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

        PatientState patient = patients.values().stream()
                .filter(existing -> !idCard.isBlank() && idCard.equals(existing.idCard))
                .findFirst()
                .orElseGet(() -> patients.values().stream()
                        .filter(existing -> idCard.isBlank())
                        .filter(existing -> patientName.equals(existing.patientName) && phone.equals(existing.phone))
                        .findFirst()
                        .orElse(null));

        if (patient == null) {
            long patientId = patientIdGenerator.getAndIncrement();
            patient = new PatientState(
                    patientId,
                    "P" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%03d", patientId),
                    patientName,
                    trimToEmpty(request.gender()),
                    idCard,
                    phone,
                    trimToEmpty(request.allergyHistory()),
                    trimToEmpty(request.pastHistory())
            );
            patients.put(patientId, patient);
        } else {
            if (!phone.isBlank()) {
                patient.phone = phone;
            }
            if (!trimToEmpty(request.allergyHistory()).isBlank()) {
                patient.allergyHistory = trimToEmpty(request.allergyHistory());
            }
            if (!trimToEmpty(request.pastHistory()).isBlank()) {
                patient.pastHistory = trimToEmpty(request.pastHistory());
            }
        }
        return toView(patient);
    }

    public synchronized PatientView findPatient(Long patientId) {
        if (jdbcRepository != null) {
            return jdbcRepository.findPatient(patientId);
        }
        return toView(requirePatient(patientId));
    }

    public List<DepartmentView> listDepartments() {
        if (jdbcRepository != null) {
            return jdbcRepository.listDepartments();
        }
        return List.copyOf(departments.values());
    }

    public List<DoctorView> listDoctors(Long deptId) {
        if (jdbcRepository != null) {
            return jdbcRepository.listDoctors(deptId);
        }
        return doctors.values().stream()
                .filter(doctor -> deptId == null || Objects.equals(doctor.deptId, deptId))
                .sorted(Comparator.comparing(doctor -> doctor.doctorId))
                .map(this::toView)
                .toList();
    }

    public List<DoctorScheduleView> listSchedules(Long deptId, Long doctorId, LocalDate workDate) {
        if (jdbcRepository != null) {
            return jdbcRepository.listSchedules(deptId, doctorId, workDate);
        }
        return schedules.values().stream()
                .filter(schedule -> deptId == null || Objects.equals(schedule.deptId, deptId))
                .filter(schedule -> doctorId == null || Objects.equals(schedule.doctorId, doctorId))
                .filter(schedule -> workDate == null || Objects.equals(schedule.workDate, workDate))
                .filter(schedule -> "可预约".equals(schedule.status))
                .filter(schedule -> schedule.remainQuota > 0)
                .sorted(Comparator
                        .comparing((ScheduleState schedule) -> schedule.workDate)
                        .thenComparing(schedule -> schedule.startTime))
                .map(this::toView)
                .toList();
    }

    public synchronized RegistrationView submitOfflineRegistration(Long patientId, Long doctorId, Long scheduleId) {
        if (jdbcRepository != null) {
            return jdbcRepository.createOfflineRegistration(patientId, doctorId, scheduleId);
        }
        return createRegistration(patientId, doctorId, scheduleId, OFFLINE, false, PENDING_PAYMENT);
    }

    public synchronized RegistrationView submitOnlineRegistration(Long patientId, Long doctorId, Long scheduleId, boolean paid) {
        if (jdbcRepository != null) {
            return jdbcRepository.createOnlineRegistration(patientId, doctorId, scheduleId, paid);
        }
        return createRegistration(patientId, doctorId, scheduleId, ONLINE, paid, paid ? WAITING : PENDING_PAYMENT);
    }

    public synchronized RegistrationView chargeRegistration(Long registrationId, String payMethod) {
        if (jdbcRepository != null) {
            return jdbcRepository.chargeRegistration(registrationId, payMethod);
        }
        RegistrationState registration = requireRegistration(registrationId);
        if (REFUNDED.equals(registration.feeStatus) || REFUNDED_QUEUE.equals(registration.status)) {
            return toView(registration);
        }
        boolean alreadyCalled = CALLED.equals(registration.status);
        registration.feeStatus = PAID;
        if (registration.queueNo == null && !ONLINE.equals(registration.source)) {
            registration.queueNo = Math.toIntExact(queueNoGenerator.getAndIncrement());
        }
        registration.status = alreadyCalled ? CALLED : WAITING;

        feeOrders.values().stream()
                .filter(feeOrder -> feeOrder.businessId.equals(registrationId))
                .filter(feeOrder -> REGISTRATION_FEE.equals(feeOrder.feeType))
                .findFirst()
                .ifPresent(feeOrder -> {
                    feeOrder.payStatus = PAID;
                    feeOrder.status = PAID;
                });

        return toView(registration);
    }

    public synchronized FeeOrderView chargeFeeOrder(Long feeOrderId, String payMethod) {
        if (jdbcRepository != null) {
            return jdbcRepository.chargeFeeOrder(feeOrderId, payMethod);
        }
        FeeOrderState feeOrder = requireFeeOrder(feeOrderId);
        if (REFUNDED.equals(feeOrder.status)) {
            throw new BusinessException("收费单已退费，不能重复收费");
        }
        feeOrder.payStatus = PAID;
        feeOrder.status = PAID;
        if (REGISTRATION_FEE.equals(feeOrder.feeType) && feeOrder.registrationId != null) {
            chargeRegistration(feeOrder.registrationId, payMethod);
        }
        return toView(feeOrder);
    }

    public synchronized RegistrationView confirmOnlineRegistration(Long registrationId) {
        if (jdbcRepository != null) {
            return jdbcRepository.confirmOnlineRegistration(registrationId);
        }
        RegistrationState registration = requireRegistration(registrationId);
        if (!ONLINE.equals(registration.source)) {
            throw new BusinessException("不是线上挂号记录");
        }
        if (!PAID.equals(registration.feeStatus)) {
            throw new BusinessException("未支付挂号费，不能确认线上挂号");
        }
        if (registration.queueNo == null) {
            registration.queueNo = Math.toIntExact(queueNoGenerator.getAndIncrement());
        }
        registration.status = WAITING;
        return toView(registration);
    }

    public synchronized List<RegistrationView> listOnlinePending() {
        if (jdbcRepository != null) {
            return jdbcRepository.listOnlinePending();
        }
        return registrations.values().stream()
                .filter(registration -> ONLINE.equals(registration.source))
                .filter(registration -> registration.queueNo == null)
                .filter(registration -> !REFUNDED.equals(registration.feeStatus))
                .filter(registration -> !REFUNDED_QUEUE.equals(registration.status))
                .sorted(Comparator.comparing(registration -> registration.registrationId))
                .map(this::toView)
                .toList();
    }

    public synchronized List<RegistrationView> listQueue(Long doctorId, Long scheduleId) {
        return listQueue(doctorId, scheduleId, null);
    }

    public synchronized List<RegistrationView> listQueue(Long doctorId, Long scheduleId, LocalDate workDate) {
        if (jdbcRepository != null) {
            return jdbcRepository.listQueue(doctorId, scheduleId, workDate);
        }
        return registrations.values().stream()
                .filter(registration -> WAITING.equals(registration.status))
                .filter(registration -> PAID.equals(registration.feeStatus))
                .filter(registration -> registration.queueNo != null)
                .filter(registration -> doctorId == null || Objects.equals(registration.doctorId, doctorId))
                .filter(registration -> scheduleId == null || Objects.equals(registration.scheduleId, scheduleId))
                .filter(registration -> {
                    ScheduleState schedule = schedules.get(registration.scheduleId);
                    return workDate == null || (schedule != null && Objects.equals(schedule.workDate, workDate));
                })
                .sorted(Comparator.comparingInt(registration -> registration.queueNo == null ? Integer.MAX_VALUE : registration.queueNo))
                .map(this::toView)
                .toList();
    }

    public synchronized RegistrationView callNext() {
        return callNext(null, null);
    }

    public synchronized RegistrationView callNext(Long doctorId, Long scheduleId) {
        return callNext(doctorId, scheduleId, null);
    }

    public synchronized RegistrationView callNext(Long doctorId, Long scheduleId, LocalDate workDate) {
        if (jdbcRepository != null) {
            return jdbcRepository.callNext(doctorId, scheduleId, workDate);
        }
        RegistrationState registration = registrations.values().stream()
                .filter(state -> PAID.equals(state.feeStatus))
                .filter(state -> WAITING.equals(state.status))
                .filter(state -> state.queueNo != null)
                .filter(state -> doctorId == null || Objects.equals(state.doctorId, doctorId))
                .filter(state -> scheduleId == null || Objects.equals(state.scheduleId, scheduleId))
                .filter(state -> {
                    ScheduleState schedule = schedules.get(state.scheduleId);
                    return workDate == null || (schedule != null && Objects.equals(schedule.workDate, workDate));
                })
                .min(Comparator.comparingInt(state -> state.queueNo == null ? Integer.MAX_VALUE : state.queueNo))
                .orElseThrow(() -> new BusinessException("挂号记录未进入待接诊队列"));
        registration.status = CALLED;
        return toView(registration);
    }

    public synchronized List<FeeOrderView> listPendingFees(Long patientId, Long registrationId) {
        if (jdbcRepository != null) {
            return jdbcRepository.listPendingFees(patientId, registrationId);
        }
        return feeOrders.values().stream()
                .filter(feeOrder -> PENDING_PAYMENT.equals(feeOrder.payStatus))
                .filter(feeOrder -> patientId == null || Objects.equals(feeOrder.patientId, patientId))
                .filter(feeOrder -> registrationId == null || Objects.equals(feeOrder.registrationId, registrationId))
                .sorted(Comparator.comparing(feeOrder -> feeOrder.feeOrderId))
                .map(this::toView)
                .toList();
    }

    public synchronized FeeHistoryView feeHistory(Long patientId, Long registrationId) {
        if (jdbcRepository != null) {
            return jdbcRepository.feeHistory(patientId, registrationId);
        }
        List<FeeOrderView> orders = feeOrders.values().stream()
                .filter(feeOrder -> patientId == null || Objects.equals(feeOrder.patientId, patientId))
                .filter(feeOrder -> registrationId == null || Objects.equals(feeOrder.registrationId, registrationId))
                .sorted(Comparator.comparing(feeOrder -> feeOrder.feeOrderId))
                .map(this::toView)
                .toList();
        return new FeeHistoryView(patientId, registrationId, orders);
    }

    public synchronized RefundCheckView checkRefund(Long feeOrderId) {
        if (jdbcRepository != null) {
            return jdbcRepository.checkRefund(feeOrderId);
        }
        FeeOrderState feeOrder = requireFeeOrder(feeOrderId);
        if (feeOrder.executed && (EXAM_FEE.equals(feeOrder.feeType) || LAB_FEE.equals(feeOrder.feeType))) {
            return new RefundCheckView(feeOrderId, false, "检查/检验项目已执行，不允许退费");
        }
        if (REGISTRATION_FEE.equals(feeOrder.feeType)) {
            RegistrationState registration = requireRegistration(feeOrder.businessId);
            if ("已取消".equals(registration.status)) {
                return new RefundCheckView(feeOrderId, false, "挂号记录已取消");
            }
            if (CALLED.equals(registration.status) || "已完成".equals(registration.status) || "爽约".equals(registration.status)) {
                return new RefundCheckView(feeOrderId, false, "已接诊患者不允许退号");
            }
        }
        if (REFUNDED.equals(feeOrder.status)) {
            return new RefundCheckView(feeOrderId, false, "收费单已退费");
        }
        return new RefundCheckView(feeOrderId, true, "可退费");
    }

    public synchronized FeeOrderView refund(Long feeOrderId, String reason) {
        if (jdbcRepository != null) {
            return jdbcRepository.refund(feeOrderId, reason);
        }
        FeeOrderState feeOrder = requireFeeOrder(feeOrderId);
        RefundCheckView check = checkRefund(feeOrderId);
        if (!check.refundable()) {
            throw new BusinessException(check.reason());
        }
        feeOrder.payStatus = REFUNDED;
        feeOrder.status = REFUNDED;
        if (REGISTRATION_FEE.equals(feeOrder.feeType)) {
            RegistrationState registration = requireRegistration(feeOrder.businessId);
            registration.feeStatus = REFUNDED;
            registration.status = REFUNDED_QUEUE;
            ScheduleState schedule = schedules.get(registration.scheduleId);
            if (schedule != null) {
                schedule.remainQuota = Math.min(schedule.totalQuota, schedule.remainQuota + 1);
            }
        }
        return toView(feeOrder);
    }

    public FeeOrderView demoExecutedExamFee() {
        return createDemoExamFee(true);
    }

    public FeeOrderView demoUnexecutedExamFee() {
        return createDemoExamFee(false);
    }

    private synchronized FeeOrderView createDemoExamFee(boolean executed) {
        long feeOrderId = feeOrderIdGenerator.getAndIncrement();
        FeeOrderState feeOrder = new FeeOrderState(feeOrderId, 1000L + feeOrderId, null, null, EXAM_FEE, PAID, PAID, executed);
        feeOrders.put(feeOrderId, feeOrder);
        return toView(feeOrder);
    }

    private RegistrationView createRegistration(
            Long patientId,
            Long doctorId,
            Long scheduleId,
            String source,
            boolean paid,
            String initialStatus
    ) {
        ScheduleState schedule = requireSchedule(scheduleId);
        if (!Objects.equals(schedule.doctorId, doctorId)) {
            throw new BusinessException("医生与排班不匹配");
        }
        if (schedule.remainQuota <= 0) {
            throw new BusinessException("当前排班暂无剩余号源");
        }
        schedule.remainQuota -= 1;

        long registrationId = registrationIdGenerator.getAndIncrement();
        String registrationNo = "REG" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%06d", registrationId);

        RegistrationState registration = new RegistrationState(
                registrationId,
                registrationNo,
                patientId,
                doctorId,
                scheduleId,
                null,
                paid ? PAID : PENDING_PAYMENT,
                initialStatus,
                source
        );
        registrations.put(registrationId, registration);

        long feeOrderId = feeOrderIdGenerator.getAndIncrement();
        feeOrders.put(feeOrderId, new FeeOrderState(
                feeOrderId,
                registrationId,
                registrationId,
                patientId,
                REGISTRATION_FEE,
                paid ? PAID : PENDING_PAYMENT,
                paid ? PAID : PENDING_PAYMENT,
                false
        ));

        return toView(registration);
    }

    private void seedCatalog() {
        departments.put(1L, new DepartmentView(1L, "REG", "挂号收费处", "REGISTRATION", "门诊一楼", "挂号收费与退费窗口"));
        departments.put(2L, new DepartmentView(2L, "GM", "全科门诊", "OUTPATIENT", "门诊二楼", "常见病、多发病首诊"));
        departments.put(3L, new DepartmentView(3L, "IMG", "医学影像科", "EXAM", "医技楼一楼", "DR、CT 等影像检查"));
        departments.put(4L, new DepartmentView(4L, "LAB", "检验科", "LAB", "医技楼二楼", "血液、尿液等检验"));
        departments.put(5L, new DepartmentView(5L, "PHA", "门诊药房", "PHARMACY", "门诊一楼", "处方发药和退药"));

        doctors.put(2L, new DoctorState(2L, 2L, "王门诊", "主治医师", "呼吸道感染、慢病随访"));

        schedules.put(1L, new ScheduleState(
                1L,
                2L,
                2L,
                LocalDate.of(2026, 6, 22),
                "上午",
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                40,
                38,
                new BigDecimal("15.00"),
                "可预约"
        ));
    }

    private PatientState requirePatient(Long patientId) {
        PatientState patient = patients.get(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return patient;
    }

    private RegistrationState requireRegistration(Long registrationId) {
        RegistrationState registration = registrations.get(registrationId);
        if (registration == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return registration;
    }

    private FeeOrderState requireFeeOrder(Long feeOrderId) {
        FeeOrderState feeOrder = feeOrders.get(feeOrderId);
        if (feeOrder == null) {
            throw new BusinessException("收费单不存在");
        }
        return feeOrder;
    }

    private ScheduleState requireSchedule(Long scheduleId) {
        ScheduleState schedule = schedules.get(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        return schedule;
    }

    private PatientView toView(PatientState patient) {
        return new PatientView(
                patient.patientId,
                patient.patientNo,
                patient.patientName,
                patient.gender,
                patient.idCard,
                patient.phone,
                patient.allergyHistory,
                patient.pastHistory
        );
    }

    private DoctorScheduleView toView(ScheduleState schedule) {
        DoctorState doctor = doctors.get(schedule.doctorId);
        DepartmentView department = departments.get(schedule.deptId);
        return new DoctorScheduleView(
                schedule.scheduleId,
                schedule.doctorId,
                doctor == null ? "" : doctor.doctorName,
                schedule.deptId,
                department == null ? "" : department.deptName(),
                schedule.workDate,
                schedule.timePeriod,
                schedule.startTime,
                schedule.endTime,
                schedule.totalQuota,
                schedule.remainQuota,
                schedule.registrationFee,
                schedule.status
        );
    }

    private DoctorView toView(DoctorState doctor) {
        DepartmentView department = departments.get(doctor.deptId);
        return new DoctorView(
                doctor.doctorId,
                doctor.doctorName,
                doctor.deptId,
                department == null ? "" : department.deptName(),
                doctor.title,
                doctor.specialty
        );
    }

    private RegistrationView toView(RegistrationState registration) {
        PatientState patient = patients.get(registration.patientId);
        DoctorState doctor = doctors.get(registration.doctorId);
        DepartmentView department = doctor == null ? null : departments.get(doctor.deptId);
        ScheduleState schedule = schedules.get(registration.scheduleId);
        return new RegistrationView(
                registration.registrationId,
                registration.registrationNo,
                registration.patientId,
                patient == null ? "" : patient.patientName,
                registration.doctorId,
                doctor == null ? "" : doctor.doctorName,
                department == null ? "" : department.deptName(),
                registration.scheduleId,
                schedule == null ? null : schedule.workDate,
                schedule == null ? "" : schedule.timePeriod,
                registration.queueNo,
                registration.feeStatus,
                registration.status
        );
    }

    private FeeOrderView toView(FeeOrderState feeOrder) {
        PatientState patient = feeOrder.patientId == null ? null : patients.get(feeOrder.patientId);
        RegistrationState registration = feeOrder.registrationId == null ? null : registrations.get(feeOrder.registrationId);
        return new FeeOrderView(
                feeOrder.feeOrderId,
                feeOrder.businessId,
                feeOrder.patientId,
                patient == null ? "" : patient.patientName,
                feeOrder.registrationId,
                feeOrder.feeType,
                BigDecimal.ZERO,
                PAID.equals(feeOrder.payStatus) ? BigDecimal.ZERO : BigDecimal.ZERO,
                REFUNDED.equals(feeOrder.payStatus) ? BigDecimal.ZERO : BigDecimal.ZERO,
                feeOrder.payStatus,
                feeOrder.executed,
                registration == null ? "" : registration.status
        );
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class PatientState {
        private final Long patientId;
        private final String patientNo;
        private final String patientName;
        private final String gender;
        private final String idCard;
        private String phone;
        private String allergyHistory;
        private String pastHistory;

        private PatientState(
                Long patientId,
                String patientNo,
                String patientName,
                String gender,
                String idCard,
                String phone,
                String allergyHistory,
                String pastHistory
        ) {
            this.patientId = patientId;
            this.patientNo = patientNo;
            this.patientName = patientName;
            this.gender = gender;
            this.idCard = idCard;
            this.phone = phone;
            this.allergyHistory = allergyHistory;
            this.pastHistory = pastHistory;
        }
    }

    private static final class DoctorState {
        private final Long doctorId;
        private final Long deptId;
        private final String doctorName;
        private final String title;
        private final String specialty;

        private DoctorState(Long doctorId, Long deptId, String doctorName, String title, String specialty) {
            this.doctorId = doctorId;
            this.deptId = deptId;
            this.doctorName = doctorName;
            this.title = title;
            this.specialty = specialty;
        }
    }

    private static final class ScheduleState {
        private final Long scheduleId;
        private final Long doctorId;
        private final Long deptId;
        private final LocalDate workDate;
        private final String timePeriod;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final int totalQuota;
        private int remainQuota;
        private final BigDecimal registrationFee;
        private final String status;

        private ScheduleState(
                Long scheduleId,
                Long doctorId,
                Long deptId,
                LocalDate workDate,
                String timePeriod,
                LocalTime startTime,
                LocalTime endTime,
                int totalQuota,
                int remainQuota,
                BigDecimal registrationFee,
                String status
        ) {
            this.scheduleId = scheduleId;
            this.doctorId = doctorId;
            this.deptId = deptId;
            this.workDate = workDate;
            this.timePeriod = timePeriod;
            this.startTime = startTime;
            this.endTime = endTime;
            this.totalQuota = totalQuota;
            this.remainQuota = remainQuota;
            this.registrationFee = registrationFee;
            this.status = status;
        }
    }

    private static final class RegistrationState {
        private final Long registrationId;
        private final String registrationNo;
        private final Long patientId;
        private final Long doctorId;
        private final Long scheduleId;
        private Integer queueNo;
        private String feeStatus;
        private String status;
        private final String source;

        private RegistrationState(
                Long registrationId,
                String registrationNo,
                Long patientId,
                Long doctorId,
                Long scheduleId,
                Integer queueNo,
                String feeStatus,
                String status,
                String source
        ) {
            this.registrationId = registrationId;
            this.registrationNo = registrationNo;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.scheduleId = scheduleId;
            this.queueNo = queueNo;
            this.feeStatus = feeStatus;
            this.status = status;
            this.source = source;
        }
    }

    private static final class FeeOrderState {
        private final Long feeOrderId;
        private final Long businessId;
        private final Long registrationId;
        private final Long patientId;
        private final String feeType;
        private String payStatus;
        private String status;
        private final boolean executed;

        private FeeOrderState(
                Long feeOrderId,
                Long businessId,
                Long registrationId,
                Long patientId,
                String feeType,
                String payStatus,
                String status,
                boolean executed
        ) {
            this.feeOrderId = feeOrderId;
            this.businessId = businessId;
            this.registrationId = registrationId;
            this.patientId = patientId;
            this.feeType = feeType;
            this.payStatus = payStatus;
            this.status = status;
            this.executed = executed;
        }
    }
}
