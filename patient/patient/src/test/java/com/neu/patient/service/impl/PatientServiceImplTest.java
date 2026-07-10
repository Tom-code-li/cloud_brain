package com.neu.patient.service.impl;

import com.neu.patient.entity.Patient;
import com.neu.patient.entity.SysRole;
import com.neu.patient.entity.SysUser;
import com.neu.patient.mapper.PatientMapper;
import com.neu.patient.mapper.SysRoleMapper;
import com.neu.patient.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private SysUser testUser;
    private Patient testPatient;
    private SysRole patientRole;

    @BeforeEach
    void setUp() {
        patientRole = new SysRole();
        patientRole.setRoleId(2L);
        patientRole.setRoleCode("PATIENT");
        patientRole.setRoleName("患者");

        testUser = new SysUser();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setPhone("13800138000");
        testUser.setRealName("测试用户");

        testPatient = new Patient();
        testPatient.setPatientId(100L);
        testPatient.setUserId(1L);
        testPatient.setPatientName("测试患者");
        testPatient.setIdCard("110101199001011234");
        testPatient.setPhone("13800138000");
    }

    // ==================== 注册 ====================

    @Test
    void testRegisterSuccess() {
        when(sysUserMapper.findByUsername("testuser")).thenReturn(null);
        when(patientMapper.countByPhone("13800138000")).thenReturn(0L);
        when(patientMapper.countByIdCard("110101199001011234")).thenReturn(0L);
        when(sysRoleMapper.findByRoleCode("PATIENT")).thenReturn(patientRole);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);
        when(patientMapper.findByPatientNo(anyString())).thenReturn(null);
        when(patientMapper.insert(any(Patient.class))).thenReturn(1);

        int result = patientService.register(testUser, testPatient);
        assertEquals(1, result);
        verify(sysUserMapper).insert(any(SysUser.class));
        verify(patientMapper).insert(any(Patient.class));
    }

    @Test
    void testRegisterUsernameExists() {
        when(sysUserMapper.findByUsername("testuser")).thenReturn(testUser);

        int result = patientService.register(testUser, testPatient);
        assertEquals(0, result);
        verify(sysUserMapper, never()).insert(Mockito.<SysUser>any());
    }

    @Test
    void testRegisterPhoneExists() {
        when(sysUserMapper.findByUsername("testuser")).thenReturn(null);
        when(patientMapper.countByPhone("13800138000")).thenReturn(1L);

        int result = patientService.register(testUser, testPatient);
        assertEquals(-2, result);
    }

    @Test
    void testRegisterIdCardExists() {
        when(sysUserMapper.findByUsername("testuser")).thenReturn(null);
        when(patientMapper.countByPhone("13800138000")).thenReturn(0L);
        when(patientMapper.countByIdCard("110101199001011234")).thenReturn(1L);

        int result = patientService.register(testUser, testPatient);
        assertEquals(-2, result);
    }

    @Test
    void testRegisterInsertUserFails() {
        when(sysUserMapper.findByUsername("testuser")).thenReturn(null);
        when(patientMapper.countByPhone("13800138000")).thenReturn(0L);
        when(patientMapper.countByIdCard("110101199001011234")).thenReturn(0L);
        when(sysRoleMapper.findByRoleCode("PATIENT")).thenReturn(patientRole);
        doReturn(0).when(sysUserMapper).insert(Mockito.<SysUser>any());

        int result = patientService.register(testUser, testPatient);
        assertEquals(-1, result);
    }

    // ==================== 登录 ====================

    @Test
    void testLoginSuccess() {
        when(sysUserMapper.loginPatient("testuser", "password123")).thenReturn(testUser);

        SysUser result = patientService.login("testuser", "password123");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoginWrongCredentials() {
        when(sysUserMapper.loginPatient("testuser", "wrongpass")).thenReturn(null);

        SysUser result = patientService.login("testuser", "wrongpass");
        assertNull(result);
    }

    // ==================== 强制登录 ====================

    @Test
    void testForceLoginSuccess() {
        when(sysUserMapper.loginPatient("testuser", "password123")).thenReturn(testUser);
        when(sysUserMapper.updateLoginStatus(1L, 1)).thenReturn(1);

        SysUser result = patientService.forceLogin("testuser", "password123");
        assertNotNull(result);
        assertEquals(1, result.getStatus());
    }

    @Test
    void testForceLoginUserNotFound() {
        when(sysUserMapper.loginPatient("testuser", "wrongpass")).thenReturn(null);

        SysUser result = patientService.forceLogin("testuser", "wrongpass");
        assertNull(result);
    }

    // ==================== 获取用户 ====================

    @Test
    void testGetUserByIdFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(testUser);

        SysUser result = patientService.getUserById(1L);
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        SysUser result = patientService.getUserById(999L);
        assertNull(result);
    }

    // ==================== 退出登录 ====================

    @Test
    void testLogoutSuccess() {
        when(sysUserMapper.updateLoginStatus(1L, 0)).thenReturn(1);

        boolean result = patientService.logout(1L);
        assertTrue(result);
    }

    @Test
    void testLogoutNullUserId() {
        boolean result = patientService.logout(null);
        assertFalse(result);
    }

    // ==================== 修改密码 ====================

    @Test
    void testChangePasswordSuccess() {
        when(sysUserMapper.selectById(1L)).thenReturn(testUser);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean result = patientService.changePassword(1L, "password123", "newpass456");
        assertTrue(result);
    }

    @Test
    void testChangePasswordUserNotFound() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        boolean result = patientService.changePassword(999L, "oldpass", "newpass");
        assertFalse(result);
    }

    @Test
    void testChangePasswordWrongOldPassword() {
        when(sysUserMapper.selectById(1L)).thenReturn(testUser);

        boolean result = patientService.changePassword(1L, "wrongold", "newpass");
        assertFalse(result);
    }

    // ==================== 获取患者信息 ====================

    @Test
    void testGetPatientInfoFound() {
        when(patientMapper.selectById(100L)).thenReturn(testPatient);

        Patient result = patientService.getPatientInfo(100L);
        assertNotNull(result);
        assertEquals("测试患者", result.getPatientName());
    }

    @Test
    void testGetPatientInfoNotFound() {
        when(patientMapper.selectById(999L)).thenReturn(null);

        Patient result = patientService.getPatientInfo(999L);
        assertNull(result);
    }

    @Test
    void testGetPatientByUserIdFound() {
        when(patientMapper.findByUserId(1L)).thenReturn(testPatient);

        Patient result = patientService.getPatientByUserId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    // ==================== 更新患者信息 ====================

    @Test
    void testUpdatePatientSuccess() {
        testPatient.setPhone("13900139000");
        when(patientMapper.updateById(any(Patient.class))).thenReturn(1);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean result = patientService.updatePatient(testPatient);
        assertTrue(result);
    }

    @Test
    void testUpdatePatientTrimEmptyFields() {
        testPatient.setEmergencyContact("");
        testPatient.setEmergencyPhone("");
        testPatient.setAddress("   ");
        testPatient.setPhone("13900139000");
        when(patientMapper.updateById(any(Patient.class))).thenReturn(1);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        boolean result = patientService.updatePatient(testPatient);
        assertTrue(result);
        assertNull(testPatient.getEmergencyContact());
        assertNull(testPatient.getAddress());
    }

    @Test
    void testUpdatePatientNoPhoneSync() {
        testPatient.setPhone(null);
        when(patientMapper.updateById(any(Patient.class))).thenReturn(1);

        boolean result = patientService.updatePatient(testPatient);
        assertTrue(result);
        verify(sysUserMapper, never()).updateById(Mockito.<SysUser>any());
    }

    // ==================== 获取当前患者 ====================

    @Test
    void testGetCurrentPatient() {
        when(patientMapper.findByUserId(1L)).thenReturn(testPatient);

        Patient result = patientService.getCurrentPatient(1L);
        assertNotNull(result);
        assertEquals(100L, result.getPatientId());
    }
}
