package com.hospital.auth.service;

import com.hospital.auth.domain.LoginResponse;
import com.hospital.common.core.BusinessException;
import com.hospital.common.security.DoctorPrincipal;
import com.hospital.common.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "账号或密码错误";
    private static final int INVALID_CREDENTIALS_CODE = 4001;
    private static final long DEFAULT_TTL_SECONDS = 3600L;

    private final JwtService jwtService;
    private final Map<String, DemoUser> users;

    private AuthService(JwtService jwtService, Map<String, DemoUser> users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Autowired
    public AuthService(@Value("${doctor.jwt.secret}") String secret,
                       @Value("${doctor.jwt.ttl-seconds}") long ttlSeconds) {
        this(new JwtService(secret, ttlSeconds), DemoUser.demoUsers());
    }

    public static AuthService demo(String secret) {
        return demo(secret, DEFAULT_TTL_SECONDS);
    }

    public static AuthService demo(String secret, long ttlSeconds) {
        return new AuthService(new JwtService(secret, ttlSeconds), DemoUser.demoUsers());
    }

    public LoginResponse login(String username, String password) {
        String normalizedUsername = normalize(username);
        String normalizedPassword = normalize(password);
        if (normalizedUsername.isEmpty() || normalizedPassword.isEmpty()) {
            throw new BusinessException(INVALID_CREDENTIALS_CODE, INVALID_CREDENTIALS_MESSAGE);
        }

        DemoUser user = users.get(normalizedUsername);
        if (user == null || !user.password().equals(normalizedPassword)) {
            throw new BusinessException(INVALID_CREDENTIALS_CODE, INVALID_CREDENTIALS_MESSAGE);
        }

        DoctorPrincipal principal = new DoctorPrincipal(
                user.userId(),
                user.doctorId(),
                user.deptId(),
                user.doctorType(),
                user.roleCode(),
                user.realName()
        );

        return new LoginResponse(
                jwtService.createToken(principal),
                user.userId(),
                user.doctorId(),
                user.deptId(),
                user.doctorType(),
                user.roleCode(),
                user.realName(),
                user.deptName()
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record DemoUser(
            Long userId,
            Long doctorId,
            Long deptId,
            String doctorType,
            String roleCode,
            String realName,
            String deptName,
            String password
    ) {
        private static Map<String, DemoUser> demoUsers() {
            DemoUser registrationDoctor = new DemoUser(1L, 1L, 1L, "挂号医生", "REGISTRATION_DOCTOR", "赵挂号", "挂号收费处", "123456");
            return Map.of(
                    "reg001", registrationDoctor,
                    "REG2025001", registrationDoctor,
                    "out001", new DemoUser(2L, 2L, 2L, "门诊医生", "OUTPATIENT_DOCTOR", "王门诊", "全科门诊", "123456"),
                    "exam001", new DemoUser(3L, 3L, 3L, "检查医生", "EXAM_DOCTOR", "钱检查", "医学影像科", "123456"),
                    "lab001", new DemoUser(4L, 4L, 4L, "检验医生", "LAB_DOCTOR", "孙检验", "检验科", "123456"),
                    "pha001", new DemoUser(5L, 5L, 5L, "药房医生", "PHARMACY_DOCTOR", "李药房", "门诊药房", "123456")
            );
        }
    }
}
