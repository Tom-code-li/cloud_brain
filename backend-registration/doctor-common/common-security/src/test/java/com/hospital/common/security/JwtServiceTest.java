package com.hospital.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void tokenRoundTripPreservesDoctorIdentity() {
        JwtService service = new JwtService("01234567890123456789012345678901", 3600);
        DoctorPrincipal principal = new DoctorPrincipal(1L, 2L, 1L, "门诊医生", "OUTPATIENT_DOCTOR", "李门诊");

        String token = service.createToken(principal);
        DoctorPrincipal parsed = service.parseToken(token);

        assertThat(parsed.userId()).isEqualTo(1L);
        assertThat(parsed.doctorId()).isEqualTo(2L);
        assertThat(parsed.deptId()).isEqualTo(1L);
        assertThat(parsed.doctorType()).isEqualTo("门诊医生");
        assertThat(parsed.roleCode()).isEqualTo("OUTPATIENT_DOCTOR");
        assertThat(parsed.realName()).isEqualTo("李门诊");
    }
}
