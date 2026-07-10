package com.neu.patient.service;

import com.neu.patient.entity.Patient;
import com.neu.patient.entity.SysUser;
import com.neu.patient.mapper.PatientMapper;
import com.neu.patient.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PatientServiceImplTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Test
    void registerCreatesPatientAccountWithGeneratedPatientNo() {
        SysUser user = new SysUser();
        user.setUsername("test_patient_register");
        user.setPassword("123456");
        user.setRealName("测试患者");
        user.setPhone("13900000000");

        Patient patient = new Patient();
        patient.setPatientName("测试患者");
        patient.setGender("男");
        patient.setPhone("13900000000");
        patient.setIdCard("110101199001011234");

        int result = patientService.register(user, patient);

        assertThat(result).isEqualTo(1);
        SysUser savedUser = sysUserMapper.findByUsername("test_patient_register");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRoleId()).isNotNull();

        Patient savedPatient = patientMapper.findByUserId(savedUser.getUserId());
        assertThat(savedPatient).isNotNull();
        assertThat(savedPatient.getPatientNo()).startsWith("P");
    }
}
