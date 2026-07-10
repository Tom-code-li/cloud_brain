package com.neu.patient.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neu.patient.common.EnumValues;
import com.neu.patient.entity.AiConsultation;
import com.neu.patient.entity.Department;
import com.neu.patient.entity.DoctorSchedule;
import com.neu.patient.entity.FeeOrder;
import com.neu.patient.entity.Patient;
import com.neu.patient.entity.Registration;
import com.neu.patient.entity.SysUser;
import com.neu.patient.mapper.AiConsultationMapper;
import com.neu.patient.mapper.DepartmentMapper;
import com.neu.patient.mapper.DoctorScheduleMapper;
import com.neu.patient.mapper.FeeOrderMapper;
import com.neu.patient.mapper.PatientMapper;
import com.neu.patient.mapper.RegistrationMapper;
import com.neu.patient.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DatabasePersistenceAuditTest {

    @Autowired private PatientService patientService;
    @Autowired private RegistrationService registrationService;
    @Autowired private FeeService feeService;
    @Autowired private AiConsultationService aiConsultationService;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private PatientMapper patientMapper;
    @Autowired private DoctorScheduleMapper doctorScheduleMapper;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private FeeOrderMapper feeOrderMapper;
    @Autowired private AiConsultationMapper aiConsultationMapper;
    @Autowired private DepartmentMapper departmentMapper;

    @Test
    void corePatientWorkflowsPersistToDatabase() {
        Patient patient = registerPatient();
        DoctorSchedule schedule = availableSchedule();

        Registration registration = new Registration();
        registration.setPatientId(patient.getPatientId());
        registration.setDeptId(schedule.getDeptId());
        registration.setDoctorId(schedule.getDoctorId());
        registration.setScheduleId(schedule.getScheduleId());
        registration.setSource(EnumValues.SOURCE_ONLINE);
        registration.setRegistrationFee(schedule.getRegistrationFee() == null ? BigDecimal.ZERO : schedule.getRegistrationFee());

        Registration savedRegistration = registrationService.registerAppointment(registration);
        Registration fromDb = registrationMapper.selectById(savedRegistration.getRegistrationId());
        FeeOrder feeOrder = feeOrderMapper.selectOne(new QueryWrapper<FeeOrder>()
                .eq("registration_id", savedRegistration.getRegistrationId())
                .eq("business_type", EnumValues.BUSINESS_REGISTRATION)
                .last("LIMIT 1"));

        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getPatientId()).isEqualTo(patient.getPatientId());
        assertThat(feeOrder).isNotNull();
        assertThat(feeOrder.getStatus()).isEqualTo(EnumValues.FEE_ORDER_WAITING_PAYMENT);

        assertThat(feeService.payFee(feeOrder.getFeeOrderId())).isTrue();
        FeeOrder paidOrder = feeOrderMapper.selectById(feeOrder.getFeeOrderId());
        Registration paidRegistration = registrationMapper.selectById(savedRegistration.getRegistrationId());
        assertThat(paidOrder.getStatus()).isEqualTo(EnumValues.FEE_ORDER_PAID);
        assertThat(paidRegistration.getFeeStatus()).isEqualTo(EnumValues.FEE_PAID);

        assertThat(registrationService.cancelRegistration(savedRegistration.getRegistrationId())).isTrue();
        FeeOrder refundedOrder = feeOrderMapper.selectById(feeOrder.getFeeOrderId());
        Registration cancelledRegistration = registrationMapper.selectById(savedRegistration.getRegistrationId());
        assertThat(refundedOrder.getStatus()).isEqualTo(EnumValues.FEE_ORDER_REFUNDED);
        assertThat(cancelledRegistration.getStatus()).isEqualTo(EnumValues.REGISTRATION_CANCELLED);
    }

    @Test
    void aiConsultationPersistsToDatabase() {
        Patient patient = registerPatient();
        Department department = departmentMapper.findAllActive().stream().findFirst().orElseThrow();
        AiConsultation consultation = new AiConsultation();
        consultation.setPatientId(patient.getPatientId());
        consultation.setChiefComplaint("审计测试咳嗽发热");
        consultation.setSymptomDetail("审计测试咳嗽发热两天");
        consultation.setAiSummary("审计测试摘要");
        consultation.setAiResult("审计测试结果");
        consultation.setRecommendedDeptId(department.getDeptId());

        consultation = aiConsultationService.createConsultation(consultation);
        AiConsultation fromDb = aiConsultationMapper.selectById(consultation.getConsultationId());

        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getPatientId()).isEqualTo(patient.getPatientId());
        assertThat(fromDb.getRecommendedDeptId()).isEqualTo(department.getDeptId());
        assertThat(fromDb.getStatus()).isEqualTo(EnumValues.AI_GENERATED);
    }

    private Patient registerPatient() {
        String suffix = String.valueOf(System.nanoTime());
        SysUser user = new SysUser();
        user.setUsername("audit_patient_" + suffix);
        user.setPassword("123456");
        user.setRealName("审计患者");
        user.setPhone("139" + suffix.substring(suffix.length() - 8));

        Patient patient = new Patient();
        patient.setPatientName("审计患者");
        patient.setGender("男");
        patient.setPhone(user.getPhone());
        patient.setIdCard("11010119900101" + suffix.substring(suffix.length() - 4));

        assertThat(patientService.register(user, patient)).isEqualTo(1);
        SysUser savedUser = sysUserMapper.findByUsername(user.getUsername());
        Patient savedPatient = patientMapper.findByUserId(savedUser.getUserId());
        assertThat(savedPatient).isNotNull();
        return savedPatient;
    }

    private DoctorSchedule availableSchedule() {
        DoctorSchedule schedule = doctorScheduleMapper.selectOne(new QueryWrapper<DoctorSchedule>()
                .eq("status", EnumValues.SCHEDULE_AVAILABLE)
                .gt("remain_quota", 0)
                .last("LIMIT 1"));
        assertThat(schedule).isNotNull();
        return schedule;
    }
}
