package com.neuCloudBrainMedical.admin.service.doctor.impl;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserCreateRequest;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserUpdateRequest;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.doctor.DoctorMapper;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorCommandService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.IScheduleQueryService;
import com.neuCloudBrainMedical.admin.service.user.IUserCommandService;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 医生命令服务。
 *
 * <p>职责：处理医生的创建、更新、状态切换、删除等写操作。
 * 通过 IUserCommandService、IUserQueryService 和 IRoleQueryService 管理关联用户与角色，
 * 通过 IScheduleQueryService 检查依赖数据，
 * 所有跨模块通信使用 UserInfo/RoleInfo DTO，而非直接传递 Entity。</p>
 */
@Service
public class DoctorCommandServiceImpl implements IDoctorCommandService {

	private static final String NOOP_PREFIX = "{noop}";
	private static final String DEFAULT_PASSWORD = "123456";

	private static final java.util.Map<String, String> ROLE_PREFIX_MAP = java.util.Map.ofEntries(
			java.util.Map.entry("DOCTOR_CLINIC", "DOC"),
			java.util.Map.entry("DOCTOR_CHIEF", "DOC"),
			java.util.Map.entry("PHARMACY", "PHAR"),
			java.util.Map.entry("PHARMACY_DOCTOR", "PHAR"),
			java.util.Map.entry("LAB_DOCTOR", "LAB"),
			java.util.Map.entry("LAB", "LAB"),
			java.util.Map.entry("EXAM_DOCTOR", "EXAM"),
			java.util.Map.entry("EXAM", "EXAM"),
			java.util.Map.entry("REGISTRATION_DOCTOR", "REG"),
			java.util.Map.entry("REGISTRATION", "REG"),
			java.util.Map.entry("OUTPATIENT", "DOC"),
			java.util.Map.entry("BILLING", "REG")
	);

	private final DoctorMapper doctorMapper;
	private final IScheduleQueryService scheduleQueryService;
	private final IUserCommandService userCommandService;
	private final IUserQueryService userQueryService;
	private final IRoleQueryService roleQueryService;
	private final IDoctorQueryService doctorQueryService;

	public DoctorCommandServiceImpl(DoctorMapper doctorMapper,
			IScheduleQueryService scheduleQueryService,
			IUserCommandService userCommandService,
			IRoleQueryService roleQueryService,
			IUserQueryService userQueryService,
			IDoctorQueryService doctorQueryService) {
		this.doctorMapper = doctorMapper;
		this.scheduleQueryService = scheduleQueryService;
		this.userCommandService = userCommandService;
		this.roleQueryService = roleQueryService;
		this.userQueryService = userQueryService;
		this.doctorQueryService = doctorQueryService;
	}

	@Override
	@Transactional
	public DoctorResponse createDoctor(DoctorCreateRequest request) {
		if (userQueryService.isPhoneTaken(request.getPhone())) {
			throw new BusinessException(400, "手机号已被使用");
		}
		if (userQueryService.isEmailTaken(request.getEmail())) {
			throw new BusinessException(400, "邮箱已被使用");
		}

		Long roleId = resolveDoctorRoleId(request.getRoleId());
		RoleInfo role = roleQueryService.findRoleById(roleId);
		String rolePrefix = ROLE_PREFIX_MAP.getOrDefault(role.getRoleCode(), "DOC");

		LocalDateTime now = LocalDateTime.now();

		// 1. 创建用户账号（使用 UserCreateRequest DTO）
		UserCreateRequest userReq = new UserCreateRequest();
		userReq.setRoleId(roleId);
		userReq.setRealName(request.getName());
		userReq.setPhone(request.getPhone());
		userReq.setEmail(request.getEmail());
		userReq.setStatus(Doctor.STATUS_ENABLED);
		// 传明文给 UserCommandServiceImpl，由其统一加 {noop} 前缀存储
		userReq.setPassword(hasText(request.getLoginPassword()) ? request.getLoginPassword() : DEFAULT_PASSWORD);
		userReq.setUsername("TEMP_" + System.currentTimeMillis());
		UserInfo userInfo = userCommandService.createUser(userReq);

		// 2. 创建医生记录
		Doctor doctor = new Doctor();
		doctor.setUserId(userInfo.getUserId());
		doctor.setDeptId(request.getDepartmentId());
		doctor.setDoctorType(request.getDoctorType() != null ? request.getDoctorType() : "OUTPATIENT");
		doctor.setTitle(request.getTitle());
		doctor.setSpecialty(request.getSpecialty());
		doctor.setIntroduction(request.getIntroduction());
		doctor.setStatus(Doctor.STATUS_ENABLED);
		doctor.setDoctorNo("TEMP_" + System.currentTimeMillis());
		doctor.setCreatedAt(now);
		doctor.setUpdatedAt(now);
		doctorMapper.insert(doctor);

		// 3. 设置正式的登录账号与医生编号
		String datePart = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
		String doctorNo = "D" + datePart + doctor.getDoctorId();
		String loginUsername = rolePrefix + datePart + doctor.getDoctorId();

		if (hasText(request.getLoginUsername())) {
			loginUsername = request.getLoginUsername();
		}

		if (userQueryService.isUsernameTaken(loginUsername, userInfo.getUserId())) {
			throw new BusinessException(400, "登录账号【" + loginUsername + "】已存在");
		}

		doctor.setDoctorNo(doctorNo);
		doctor.setUpdatedAt(now);
		doctorMapper.updateById(doctor);

		UserUpdateRequest updateUser = new UserUpdateRequest();
		updateUser.setUserId(userInfo.getUserId());
		updateUser.setUsername(loginUsername);
		userCommandService.updateUser(updateUser);

		return doctorQueryService.getDoctorDetail(doctor.getDoctorId());
	}

	@Override
	@Transactional
	public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest req) {
		Doctor doctor = doctorMapper.selectById(id);
		if (doctor == null) {
			throw new BusinessException(404, "医生不存在");
		}

		boolean doctorChanged = applyDoctorUpdates(doctor, req);
		if (doctorChanged) {
			doctor.setUpdatedAt(LocalDateTime.now());
			doctorMapper.updateById(doctor);
		}

		if (doctor.getUserId() != null) {
			UserInfo existing = userQueryService.findUserById(doctor.getUserId());
			UserUpdateRequest updateReq = buildUserUpdate(doctor.getUserId(), existing, req);
			if (updateReq != null) {
				userCommandService.updateUser(updateReq);
			}
		}

		return doctorQueryService.getDoctorDetail(id);
	}

	@Override
	public DoctorDisableCheckResponse checkBeforeDisable(Long id) {
		long pendingSchedules = scheduleQueryService.countActiveSchedulesByDoctor(id);
		long pendingRegistrations = scheduleQueryService.countPendingRegistrationsByDoctor(id);

		return new DoctorDisableCheckResponse(pendingSchedules, pendingRegistrations);
	}

	@Override
	@Transactional
	public DoctorResponse toggleStatus(Long id, boolean force) {
		Doctor doctor = doctorMapper.selectById(id);
		if (doctor == null) {
			throw new BusinessException(404, "医生不存在");
		}

		if (doctor.getStatus() == Doctor.STATUS_ENABLED) {
			DoctorDisableCheckResponse check = checkBeforeDisable(id);
			if (check.hasPending() && !force) {
				throw new BusinessException(409,
						"该医生仍有 " + check.getPendingSchedules() + " 个排班和 "
								+ check.getPendingRegistrations() + " 个挂号未完成，请确认是否仍要禁用");
			}
			doctor.setStatus(Doctor.STATUS_DISABLED);
		} else {
			doctor.setStatus(Doctor.STATUS_ENABLED);
		}
		doctor.setUpdatedAt(LocalDateTime.now());
		doctorMapper.updateById(doctor);

		syncUserStatus(doctor);

		return doctorQueryService.getDoctorDetail(id);
	}

	private Long resolveDoctorRoleId(Long requestedRoleId) {
		if (requestedRoleId != null) {
			RoleInfo role = roleQueryService.findRoleById(requestedRoleId);
			if (role == null) {
				throw new BusinessException(400, "所选角色不存在");
			}
			return role.getRoleId();
		}

		RoleInfo defaultRole = roleQueryService.findRoleByCode("DOCTOR_CLINIC");
		if (defaultRole != null) {
			return defaultRole.getRoleId();
		}

		List<RoleInfo> allDoctor = roleQueryService.findRolesByCodeLike("DOCTOR");
		if (allDoctor.isEmpty()) {
			throw new BusinessException(500, "系统中尚未配置任何医生角色");
		}
		return allDoctor.get(0).getRoleId();
	}

	private boolean applyDoctorUpdates(Doctor d, DoctorUpdateRequest req) {
		boolean changed = false;
		if (needsUpdate(req.getDepartmentId(), d.getDeptId())) { d.setDeptId(req.getDepartmentId()); changed = true; }
		if (needsUpdate(req.getDoctorType(), d.getDoctorType())) { d.setDoctorType(req.getDoctorType()); changed = true; }
		if (needsUpdate(req.getTitle(), d.getTitle())) { d.setTitle(req.getTitle()); changed = true; }
		if (needsUpdate(req.getSpecialty(), d.getSpecialty())) { d.setSpecialty(req.getSpecialty()); changed = true; }
		if (needsUpdate(req.getIntroduction(), d.getIntroduction())) { d.setIntroduction(req.getIntroduction()); changed = true; }
		return changed;
	}

	/** 构建用户更新请求，对比 DoctorUpdateRequest 与现有 UserInfo，仅包含真正变更的字段。 */
	private UserUpdateRequest buildUserUpdate(Long userId, UserInfo existing, DoctorUpdateRequest req) {
		if (existing == null) return null;

		UserUpdateRequest update = new UserUpdateRequest();
		update.setUserId(userId);
		boolean changed = false;

		if (needsUpdate(req.getName(), existing.getRealName())) {
			update.setRealName(req.getName());
			changed = true;
		}
		if (needsUpdate(req.getPhone(), existing.getPhone())) {
			update.setPhone(req.getPhone());
			changed = true;
		}
		if (needsUpdate(req.getEmail(), existing.getEmail())) {
			update.setEmail(req.getEmail());
			changed = true;
		}
		if (hasText(req.getLoginUsername()) && !req.getLoginUsername().equals(existing.getUsername())) {
			if (userQueryService.isUsernameTaken(req.getLoginUsername(), userId)) {
				throw new BusinessException(400, "登录账号【" + req.getLoginUsername() + "】已存在");
			}
			update.setUsername(req.getLoginUsername());
			changed = true;
		}
		if (hasText(req.getLoginPassword())) {
			update.setPassword(req.getLoginPassword());
			changed = true;
		}
		if (req.getRoleId() != null && !req.getRoleId().equals(existing.getRoleId())) {
			RoleInfo role = roleQueryService.findRoleById(req.getRoleId());
			if (role == null) {
				throw new BusinessException(400, "所选角色不存在");
			}
			update.setRoleId(req.getRoleId());
			changed = true;
		}

		return changed ? update : null;
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}

	private static <T> boolean needsUpdate(T incoming, T existing) {
		return incoming != null && !Objects.equals(incoming, existing);
	}

	@Override
	@Transactional
	public void deleteDoctor(Long id) {
		Doctor doctor = doctorMapper.selectById(id);
		if (doctor == null) {
			throw new BusinessException(404, "医生不存在");
		}
		// 删除医生记录
		doctorMapper.deleteById(id);
		// 删除关联用户账号
		if (doctor.getUserId() != null) {
			userCommandService.deleteUser(doctor.getUserId());
		}
	}

	private void syncUserStatus(Doctor doctor) {
		if (doctor.getUserId() != null) {
			UserInfo user = userQueryService.findUserById(doctor.getUserId());
			if (user != null && !Objects.equals(user.getStatus(), doctor.getStatus())) {
				UserUpdateRequest update = new UserUpdateRequest();
				update.setUserId(doctor.getUserId());
				update.setStatus(doctor.getStatus());
				userCommandService.updateUser(update);
			}
		}
	}
}
