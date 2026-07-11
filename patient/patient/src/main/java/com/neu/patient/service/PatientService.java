package com.neu.patient.service;

import com.neu.patient.entity.*;

public interface PatientService {
    // 患者注册
    int register(SysUser user, Patient patient);
    // 患者登录
    SysUser login(String username, String password);
    // 强制接管登录
    SysUser forceLogin(String username, String password);
    // 按ID获取账号
    SysUser getUserById(Long userId);
    // 退出登录
    boolean logout(Long userId);
    // 修改登录密码
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    // 获取患者信息
    Patient getPatientInfo(Long patientId);
    Patient getPatientByUserId(Long userId);
    // 更新患者信息
    boolean updatePatient(Patient patient);
    // 获取当前登录患者
    Patient getCurrentPatient(Long userId);
}
