package com.neu.patient.service.impl;

import com.neu.patient.entity.Patient;
import com.neu.patient.entity.SysRole;
import com.neu.patient.entity.SysUser;
import com.neu.patient.mapper.PatientMapper;
import com.neu.patient.mapper.SysRoleMapper;
import com.neu.patient.mapper.SysUserMapper;
import com.neu.patient.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PatientServiceImpl implements PatientService {
    private static final String PATIENT_ROLE_CODE = "PATIENT";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    @Transactional
    public int register(SysUser user, Patient patient) {
        SysUser existing = sysUserMapper.findByUsername(user.getUsername());
        if (existing != null) {
            return 0;
        }
        Long phoneCount = patientMapper.countByPhone(user.getPhone());
        if (phoneCount != null && phoneCount > 0) {
            return -2;
        }
        Long idCardCount = patientMapper.countByIdCard(patient.getIdCard());
        if (idCardCount != null && idCardCount > 0) {
            return -2;
        }

        user.setRoleId(getPatientRoleId());
        user.setStatus(1);
        int result = sysUserMapper.insert(user);
        if (result > 0) {
            patient.setUserId(user.getUserId());
            patient.setPatientNo(generatePatientNo());
            patient.setStatus(1);
            patientMapper.insert(patient);
            return 1;
        }
        return -1;
    }

    @Override
    public SysUser login(String username, String password) {
        return sysUserMapper.loginPatient(username, password);
    }

    @Override
    @Transactional
    public SysUser forceLogin(String username, String password) {
        SysUser user = sysUserMapper.loginPatient(username, password);
        if (user == null) {
            return null;
        }
        sysUserMapper.updateLoginStatus(user.getUserId(), 1);
        user.setStatus(1);
        return user;
    }

    @Override
    public SysUser getUserById(Long userId) {
        return sysUserMapper.selectById(userId);
    }

    @Override
    @Transactional
    public boolean logout(Long userId) {
        if (userId == null) {
            return false;
        }
        return sysUserMapper.updateLoginStatus(userId, 0) > 0;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if (oldPassword == null || !oldPassword.equals(user.getPassword())) {
            return false;
        }
        user.setPassword(newPassword);
        user.setUpdatedAt(LocalDateTime.now());
        return sysUserMapper.updateById(user) > 0;
    }

    @Override
    public Patient getPatientInfo(Long patientId) {
        return patientMapper.selectById(patientId);
    }

    @Override
    public Patient getPatientByUserId(Long userId) {
        return patientMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean updatePatient(Patient patient) {
        if (patient.getEmergencyContact() != null && patient.getEmergencyContact().trim().isEmpty()) {
            patient.setEmergencyContact(null);
        }
        if (patient.getEmergencyPhone() != null && patient.getEmergencyPhone().trim().isEmpty()) {
            patient.setEmergencyPhone(null);
        }
        if (patient.getAddress() != null && patient.getAddress().trim().isEmpty()) {
            patient.setAddress(null);
        }
        patient.setUpdatedAt(LocalDateTime.now());
        boolean updated = patientMapper.updateById(patient) > 0;
        if (updated && patient.getUserId() != null && patient.getPhone() != null) {
            SysUser user = new SysUser();
            user.setUserId(patient.getUserId());
            user.setPhone(patient.getPhone());
            user.setUpdatedAt(LocalDateTime.now());
            sysUserMapper.updateById(user);
        }
        return updated;
    }

    @Override
    public Patient getCurrentPatient(Long userId) {
        return patientMapper.findByUserId(userId);
    }

    private Long getPatientRoleId() {
        SysRole role = sysRoleMapper.findByRoleCode(PATIENT_ROLE_CODE);
        if (role == null) {
            role = new SysRole();
            role.setRoleCode(PATIENT_ROLE_CODE);
            role.setRoleName("患者");
            role.setDescription("患者端用户");
            role.setStatus(1);
            sysRoleMapper.insert(role);
        }
        return role.getRoleId();
    }

    private String generatePatientNo() {
        String patientNo;
        do {
            patientNo = "P" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (patientMapper.findByPatientNo(patientNo) != null);
        return patientNo;
    }
}
