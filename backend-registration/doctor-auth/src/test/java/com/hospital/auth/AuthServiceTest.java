package com.hospital.auth;

import com.hospital.auth.domain.LoginResponse;
import com.hospital.auth.service.AuthService;
import com.hospital.common.core.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AuthServiceTest {
    @Test
    void demoLoginReturnsDoctorProfileAndToken() {
        AuthService service = AuthService.demo("01234567890123456789012345678901");

        LoginResponse response = service.login("out001", "123456");

        assertThat(response.token()).isNotBlank();
        assertThat(response.doctorType()).isEqualTo("门诊医生");
        assertThat(response.roleCode()).isEqualTo("OUTPATIENT_DOCTOR");
        assertThat(response.realName()).isEqualTo("王门诊");
        assertThat(response.deptName()).isEqualTo("全科门诊");
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthService service = AuthService.demo("01234567890123456789012345678901");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> service.login("out001", "bad"))
                .satisfies(exception -> assertThat(exception.getCode()).isEqualTo(4001))
                .withMessage("账号或密码错误");
    }

    @Test
    void loginRejectsWrongUsername() {
        AuthService service = AuthService.demo("01234567890123456789012345678901");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> service.login("missing", "123456"))
                .satisfies(exception -> assertThat(exception.getCode()).isEqualTo(4001))
                .withMessage("账号或密码错误");
    }

    @Test
    void loginRejectsNullUsername() {
        AuthService service = AuthService.demo("01234567890123456789012345678901");

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> service.login(null, "123456"))
                .satisfies(exception -> assertThat(exception.getCode()).isEqualTo(4001))
                .withMessage("账号或密码错误");
    }
}
