package com.doctor.platform.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.auth.dto.LoginRequest;
import com.doctor.platform.auth.dto.LoginResponse;
import com.doctor.platform.auth.entity.Doctor;
import com.doctor.platform.auth.entity.SysRole;
import com.doctor.platform.auth.entity.SysUser;
import com.doctor.platform.auth.mapper.DoctorMapper;
import com.doctor.platform.auth.mapper.SysRoleMapper;
import com.doctor.platform.auth.mapper.SysUserMapper;
import com.doctor.platform.infrastructure.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private static final Map<String, String> WORKBENCH_ROUTES = Map.of(
        "outpatient", "/patients",
        "registration", "/registration",
        "exam", "/exam-workbench",
        "lab", "/lab-workbench",
        "pharmacy", "/pharmacy-workbench"
    );
    private static final Set<String> OUTPATIENT_ROLE_CODES = Set.of("outpatient", "OUTPATIENT_DOCTOR");
    private static final Set<String> REGISTRATION_ROLE_CODES = Set.of("registration", "REGISTRATION_DOCTOR");
    private static final Set<String> EXAM_ROLE_CODES = Set.of("exam", "EXAM_DOCTOR");
    private static final Set<String> LAB_ROLE_CODES = Set.of("lab", "LAB_DOCTOR");
    private static final Set<String> PHARMACY_ROLE_CODES = Set.of("pharmacy", "PHARMACY_DOCTOR");

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final DoctorMapper doctorMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper,
                       SysRoleMapper sysRoleMapper,
                       DoctorMapper doctorMapper,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.doctorMapper = doctorMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "账号不存在或已停用");
        }
        if (!matchesPassword(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }

        SysRole role = sysRoleMapper.selectById(user.getRoleId());
        if (role == null || !StringUtils.hasText(role.getRoleCode())) {
            throw new BusinessException(403, "用户角色未配置");
        }

        Doctor doctor = doctorMapper.selectOne(
            new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, user.getUserId())
        );

        String roleCode = normalizeRoleCode(role.getRoleCode());
        String workbenchRoute = WORKBENCH_ROUTES.getOrDefault(roleCode, "/");
        String token = jwtService.generateToken(user.getUserId(), user.getUsername(), roleCode);

        return LoginResponse.builder()
            .token(token)
            .roleCode(roleCode)
            .roleName(role.getRoleName())
            .workbenchRoute(workbenchRoute)
            .user(LoginResponse.UserProfile.builder()
                .userId(user.getUserId())
                .doctorId(doctor != null ? doctor.getDoctorId() : null)
                .username(user.getUsername())
                .realName(user.getRealName())
                .doctorNo(doctor != null ? doctor.getDoctorNo() : null)
                .build())
            .build();
    }

    private boolean matchesPassword(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(encodedPassword)) {
            return false;
        }
        if (encodedPassword.startsWith("{noop}")) {
            return rawPassword.equals(encodedPassword.substring("{noop}".length()));
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private String normalizeRoleCode(String roleCode) {
        if (OUTPATIENT_ROLE_CODES.contains(roleCode)) {
            return "outpatient";
        }
        if (REGISTRATION_ROLE_CODES.contains(roleCode)) {
            return "registration";
        }
        if (EXAM_ROLE_CODES.contains(roleCode)) {
            return "exam";
        }
        if (LAB_ROLE_CODES.contains(roleCode)) {
            return "lab";
        }
        if (PHARMACY_ROLE_CODES.contains(roleCode)) {
            return "pharmacy";
        }
        return roleCode;
    }
}
