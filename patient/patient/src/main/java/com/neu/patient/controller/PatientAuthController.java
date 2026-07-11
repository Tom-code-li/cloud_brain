package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.Patient;
import com.neu.patient.entity.SysUser;
import com.neu.patient.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/patient")
public class PatientAuthController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody LoginRequest req) {
        SysUser user = patientService.login(req.getUsername(), req.getPassword());
        if (user != null) {
            SysUser dbUser = patientService.getUserById(user.getUserId());
            Integer currentStatus = dbUser == null ? null : dbUser.getStatus();
            if (currentStatus != null && currentStatus == 1) {
                return Result.fail("当前帐号已在别处登录，是否在本设备登录，别处账号将自动退出");
            }
            patientService.forceLogin(req.getUsername(), req.getPassword());
            user.setStatus(1);
            return Result.ok("登录成功", user);
        }
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/login-force")
    public Result<SysUser> loginForce(@RequestBody LoginRequest req) {
        SysUser user = patientService.forceLogin(req.getUsername(), req.getPassword());
        if (user != null) {
            return Result.ok("登录成功", user);
        }
        return Result.fail("用户名或密码错误");
    }

    @PostMapping("/logout/{userId}")
    public Result<?> logout(@PathVariable Long userId) {
        return patientService.logout(userId) ? Result.ok("退出成功") : Result.fail("退出失败");
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest req) {
        String validationMessage = validateRegisterRequest(req);
        if (validationMessage != null) {
            return Result.fail(validationMessage);
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername().trim());
        user.setPassword(req.getPassword());
        user.setRealName(req.getRealName().trim());
        user.setPhone(req.getPhone().trim());

        Patient patient = new Patient();
        patient.setPatientName(req.getRealName().trim());
        patient.setGender(req.getGender());
        patient.setBirthday(req.getBirthday());
        patient.setPhone(req.getPhone().trim());
        patient.setIdCard(req.getIdCard().trim());
        patient.setEmergencyContact(trimToNull(req.getEmergencyContact()));
        patient.setEmergencyPhone(trimToNull(req.getEmergencyPhone()));
        patient.setAddress(trimToNull(req.getAddress()));
        patient.setAllergyHistory(isBlank(req.getAllergyHistory()) ? "无" : req.getAllergyHistory().trim());

        int result = patientService.register(user, patient);
        switch (result) {
            case 1: return Result.ok("注册成功，请登录");
            case 0: return Result.fail("用户名已存在");
            case -2: return Result.fail("您已注册过帐号，请直接登录");
            default: return Result.fail("注册失败");
        }
    }

    @GetMapping("/info/{userId}")
    public Result<Patient> getPatientInfo(@PathVariable Long userId) {
        Patient patient = patientService.getPatientByUserId(userId);
        return patient != null ? Result.ok(patient) : Result.fail("未找到患者信息");
    }

    @PutMapping("/update")
    public Result<?> updatePatient(@RequestBody Patient patient) {
        return patientService.updatePatient(patient) ? Result.ok("修改成功") : Result.fail("修改失败");
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody ChangePasswordRequest req) {
        if (req.getUserId() == null) return Result.fail("登录信息已失效，请重新登录");
        if (isBlank(req.getOldPassword())) return Result.fail("请输入原密码");
        if (isBlank(req.getNewPassword())) return Result.fail("请输入新密码");
        boolean changed = patientService.changePassword(req.getUserId(), req.getOldPassword().trim(), req.getNewPassword().trim());
        return changed ? Result.ok("密码修改成功，请重新登录") : Result.fail("原密码不正确");
    }

    private String validateRegisterRequest(RegisterRequest req) {
        if (isBlank(req.getUsername())) return "请输入用户名";
        if (isBlank(req.getPassword())) return "请输入密码";
        if (isBlank(req.getRealName())) return "请输入真实姓名";
        if (isBlank(req.getPhone())) return "请输入手机号";
        if (isBlank(req.getIdCard())) return "请输入身份证号";
        if (isBlank(req.getGender())) return "请选择性别";
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private LocalDate birthday;
        private String emergencyContact;
        private String emergencyPhone;
        private String gender;
        private String idCard;
        private String address;
        private String allergyHistory;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public LocalDate getBirthday() { return birthday; }
        public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
        public String getEmergencyContact() { return emergencyContact; }
        public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
        public String getEmergencyPhone() { return emergencyPhone; }
        public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getAllergyHistory() { return allergyHistory; }
        public void setAllergyHistory(String allergyHistory) { this.allergyHistory = allergyHistory; }
    }

    static class ChangePasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
