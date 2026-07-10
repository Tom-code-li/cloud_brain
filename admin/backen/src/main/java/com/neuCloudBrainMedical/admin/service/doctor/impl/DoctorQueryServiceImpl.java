package com.neuCloudBrainMedical.admin.service.doctor.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuCloudBrainMedical.admin.dto.PageResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.role.RoleInfo;
import com.neuCloudBrainMedical.admin.dto.user.UserInfo;
import com.neuCloudBrainMedical.admin.entity.doctor.Doctor;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.doctor.DoctorMapper;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.service.user.IUserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 医生查询服务（读操作）。
 *
 * <p>通过 IUserQueryService / IRoleQueryService / IDepartmentQueryService 获取关联数据
 * （UserInfo / RoleInfo / DepartmentResponse DTO），而非直接操作底层 Mapper 或访问 Entity，
 * 维护分层边界。</p>
 */
@Service
@Transactional(readOnly = true)
public class DoctorQueryServiceImpl implements IDoctorQueryService {

	private final DoctorMapper doctorMapper;
	private final IDepartmentQueryService departmentQueryService;
	private final IUserQueryService userQueryService;
	private final IRoleQueryService roleQueryService;

	public DoctorQueryServiceImpl(DoctorMapper doctorMapper,
			IDepartmentQueryService departmentQueryService,
			IUserQueryService userQueryService,
			IRoleQueryService roleQueryService) {
		this.doctorMapper = doctorMapper;
		this.departmentQueryService = departmentQueryService;
		this.userQueryService = userQueryService;
		this.roleQueryService = roleQueryService;
	}

	@Override
	public List<DoctorRoleOption> listDoctorRoles() {
		// 查询包含 DOCTOR 的角色（可用状态）
		List<RoleInfo> doctorRoles = roleQueryService.findRolesByCodeLike("DOCTOR");
		return doctorRoles.stream()
				.filter(r -> r.getStatus() != null && r.getStatus() == 1)
				.map(r -> new DoctorRoleOption(r.getRoleId(), r.getRoleCode(), r.getRoleName(), r.getDescription()))
				.toList();
	}

	@Override
	public PageResponse<DoctorResponse> listDoctors(Long departmentId, String keyword,
			Integer status, String title,
			int pageNum, int pageSize) {
		LambdaQueryWrapper<Doctor> wrapper = buildListWrapper(departmentId, keyword, status, title);

		Page<Doctor> page = new Page<>(pageNum, pageSize);
		List<Doctor> doctors = doctorMapper.selectPage(page, wrapper).getRecords();

		if (doctors.isEmpty()) {
			return PageResponse.of(page.getTotal(), pageNum, pageSize, Collections.emptyList());
		}

		Set<Long> deptIds = new HashSet<>();
		Set<Long> userIds = new HashSet<>();
		for (Doctor d : doctors) {
			if (d.getDeptId() != null) deptIds.add(d.getDeptId());
			if (d.getUserId() != null) userIds.add(d.getUserId());
		}

		Map<Long, DepartmentResponse> deptMap = deptIds.isEmpty()
				? Collections.emptyMap()
				: departmentQueryService.findDepartmentResponsesByIds(deptIds);

		Map<Long, UserInfo> userMap = userQueryService.findUsersByIds(userIds);
		Set<Long> roleIds = new HashSet<>();
		for (UserInfo u : userMap.values()) {
			if (u.getRoleId() != null) roleIds.add(u.getRoleId());
		}

		Map<Long, String> roleNames = new HashMap<>();
		for (Long rid : roleIds) {
			try {
				RoleInfo r = roleQueryService.findRoleById(rid);
				roleNames.put(rid, r.getRoleName());
			} catch (BusinessException ex) {
				// 角色不存在，跳过
			}
		}

		List<DoctorResponse> responses = new ArrayList<>();
		for (Doctor d : doctors) {
			DoctorResponse resp = new DoctorResponse();
			resp.setDoctorId(d.getDoctorId());
			resp.setDoctorNo(d.getDoctorNo());
			resp.setDepartmentId(d.getDeptId());
			DepartmentResponse dept = deptMap.get(d.getDeptId());
			if (dept != null) {
				resp.setDepartmentName(dept.getName());
			}
			resp.setDoctorType(doctorTypeToChinese(d.getDoctorType()));
			resp.setTitle(d.getTitle());
			resp.setSpecialty(d.getSpecialty());
			UserInfo user = userMap.get(d.getUserId());
			if (user != null) {
				resp.setDoctorName(user.getRealName());
				resp.setLoginUsername(user.getUsername());
				resp.setPhone(user.getPhone());
				resp.setEmail(user.getEmail());
				if (user.getRoleId() != null) {
					resp.setRoleId(user.getRoleId());
					resp.setRoleName(roleNames.get(user.getRoleId()));
				}
			}
			resp.setStatus(d.getStatus());
			resp.setCreatedAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
			resp.setUpdatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : null);
			responses.add(resp);
		}

		return PageResponse.of(page.getTotal(), pageNum, pageSize, responses);
	}

	@Override
	public DoctorResponse getDoctorDetail(Long id) {
		Doctor doctor = doctorMapper.selectById(id);
		if (doctor == null) {
			throw new BusinessException(404, "医生不存在");
		}

		DoctorResponse resp = new DoctorResponse();
		resp.setDoctorId(doctor.getDoctorId());
		resp.setDoctorNo(doctor.getDoctorNo());
		resp.setDoctorType(doctorTypeToChinese(doctor.getDoctorType()));
		resp.setTitle(doctor.getTitle());
		resp.setSpecialty(doctor.getSpecialty());
		resp.setIntroduction(doctor.getIntroduction());
		if (doctor.getDeptId() != null) {
			resp.setDepartmentId(doctor.getDeptId());
			try {
				DepartmentResponse dept = departmentQueryService.getDepartmentDetail(doctor.getDeptId());
				resp.setDepartmentName(dept.getName());
			} catch (BusinessException ignored) {
			}
		}

		if (doctor.getUserId() != null) {
			UserInfo user = userQueryService.findUserById(doctor.getUserId());
			if (user != null) {
				resp.setDoctorName(user.getRealName());
				resp.setLoginUsername(user.getUsername());
				resp.setPhone(user.getPhone());
				resp.setEmail(user.getEmail());
				if (user.getRoleId() != null) {
					resp.setRoleId(user.getRoleId());
					try {
						RoleInfo role = roleQueryService.findRoleById(user.getRoleId());
						resp.setRoleName(role.getRoleName());
					} catch (BusinessException ignored) {
					}
				}
			}
		}

		resp.setStatus(doctor.getStatus());
		resp.setCreatedAt(doctor.getCreatedAt() != null ? doctor.getCreatedAt().toString() : null);
		resp.setUpdatedAt(doctor.getUpdatedAt() != null ? doctor.getUpdatedAt().toString() : null);
		return resp;
	}

	@Override
	public List<DoctorResponse> exportDoctors(Long departmentId) {
		LambdaQueryWrapper<Doctor> wrapper = buildListWrapper(departmentId, null, null, null);
		List<Doctor> doctors = doctorMapper.selectList(wrapper);
		if (doctors.isEmpty()) return Collections.emptyList();

		Set<Long> deptIds = new HashSet<>();
		Set<Long> userIds = new HashSet<>();
		for (Doctor d : doctors) {
			if (d.getDeptId() != null) deptIds.add(d.getDeptId());
			if (d.getUserId() != null) userIds.add(d.getUserId());
		}
		Map<Long, DepartmentResponse> deptMap = deptIds.isEmpty()
				? Collections.emptyMap()
				: departmentQueryService.findDepartmentResponsesByIds(deptIds);
		Map<Long, UserInfo> userMap = userQueryService.findUsersByIds(userIds);

		List<DoctorResponse> result = new ArrayList<>();
		for (Doctor doctor : doctors) {
			DoctorResponse resp = new DoctorResponse();
			resp.setDoctorId(doctor.getDoctorId());
			resp.setDoctorNo(doctor.getDoctorNo());
			resp.setDoctorType(doctorTypeToChinese(doctor.getDoctorType()));
			resp.setTitle(doctor.getTitle());
			resp.setSpecialty(doctor.getSpecialty());
			resp.setStatus(doctor.getStatus());
			UserInfo user = userMap.get(doctor.getUserId());
			if (user != null) {
				resp.setDoctorName(user.getRealName());
				resp.setPhone(user.getPhone());
			}
			DepartmentResponse dept = deptMap.get(doctor.getDeptId());
			if (dept != null) resp.setDepartmentName(dept.getName());
			result.add(resp);
		}
		return result;
	}

	@Override
	public List<DoctorOptionDTO> listEnabledDoctors(Long departmentId) {
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Doctor::getStatus, Doctor.STATUS_ENABLED);
		if (departmentId != null) {
			wrapper.eq(Doctor::getDeptId, departmentId);
		}
		wrapper.orderByAsc(Doctor::getDoctorId);
		List<Doctor> doctors = doctorMapper.selectList(wrapper);

		Set<Long> userIds = new HashSet<>();
		for (Doctor d : doctors) {
			if (d.getUserId() != null) userIds.add(d.getUserId());
		}
		Map<Long, UserInfo> userMap = userQueryService.findUsersByIds(userIds);

		List<DoctorOptionDTO> result = new ArrayList<>();
		for (Doctor doctor : doctors) {
			DoctorOptionDTO dto = new DoctorOptionDTO();
			dto.setDoctorId(doctor.getDoctorId());
			dto.setDoctorNo(doctor.getDoctorNo());
			dto.setDoctorType(doctorTypeToChinese(doctor.getDoctorType()));
			dto.setTitle(doctor.getTitle());
			dto.setSpecialty(doctor.getSpecialty());
			UserInfo user = userMap.get(doctor.getUserId());
			if (user != null) {
				dto.setDoctorName(user.getRealName());
			}
			if (dto.getDoctorName() == null || dto.getDoctorName().isBlank()) {
				dto.setDoctorName("医生" + doctor.getDoctorId());
			}
			if (doctor.getTitle() != null && !doctor.getTitle().isBlank()) {
				dto.setDoctorName(dto.getDoctorName() + "（" + doctor.getTitle() + "）");
			}
			result.add(dto);
		}
		return result;
	}

	@Override
	public Map<Long, DoctorInfo> findDoctorInfoByIds(Set<Long> doctorIds) {
		if (doctorIds == null || doctorIds.isEmpty()) {
			return Collections.emptyMap();
		}
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(Doctor::getDoctorId, doctorIds);
		List<Doctor> doctors = doctorMapper.selectList(wrapper);

		Set<Long> userIds = new HashSet<>();
		for (Doctor d : doctors) {
			if (d.getUserId() != null) userIds.add(d.getUserId());
		}
		Map<Long, UserInfo> userMap = userQueryService.findUsersByIds(userIds);

		Map<Long, DoctorInfo> result = new HashMap<>();
		for (Doctor d : doctors) {
			DoctorInfo info = new DoctorInfo();
			info.setDoctorId(d.getDoctorId());
			info.setDoctorNo(d.getDoctorNo());
			info.setDoctorType(doctorTypeToChinese(d.getDoctorType()));
			info.setTitle(d.getTitle());
			info.setSpecialty(d.getSpecialty());
			info.setDeptId(d.getDeptId());
			info.setUserId(d.getUserId());
			UserInfo user = userMap.get(d.getUserId());
			if (user != null) info.setDoctorName(user.getRealName());
			result.put(d.getDoctorId(), info);
		}
		return result;
	}

	@Override
	public List<DoctorInfo> listEnabledDoctorInfosByDepartment(Long departmentId) {
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Doctor::getDeptId, departmentId)
				.eq(Doctor::getStatus, Doctor.STATUS_ENABLED)
				.orderByAsc(Doctor::getDoctorId);
		List<Doctor> doctors = doctorMapper.selectList(wrapper);

		if (doctors.isEmpty()) {
			return Collections.emptyList();
		}

		Set<Long> userIds = new HashSet<>();
		for (Doctor d : doctors) {
			if (d.getUserId() != null) userIds.add(d.getUserId());
		}
		Map<Long, UserInfo> userMap = userQueryService.findUsersByIds(userIds);

		List<DoctorInfo> result = new ArrayList<>();
		for (Doctor doctor : doctors) {
			DoctorInfo info = new DoctorInfo();
			info.setDoctorId(doctor.getDoctorId());
			info.setDoctorNo(doctor.getDoctorNo());
			info.setDoctorType(doctorTypeToChinese(doctor.getDoctorType()));
			info.setTitle(doctor.getTitle());
			info.setSpecialty(doctor.getSpecialty());
			info.setDeptId(doctor.getDeptId());
			info.setUserId(doctor.getUserId());
			UserInfo user = userMap.get(doctor.getUserId());
			if (user != null) info.setDoctorName(user.getRealName());
			result.add(info);
		}
		return result;
	}

	@Override
	public long countActiveDoctorsByDepartment(Long deptId) {
		if (deptId == null) return 0L;
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Doctor::getDeptId, deptId);
		Long count = doctorMapper.selectCount(wrapper);
		return count != null ? count : 0L;
	}

	@Override
	public Map<Long, Long> getActiveDoctorCountByDepartmentMap() {
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Doctor::getStatus, Doctor.STATUS_ENABLED);
		java.util.List<Doctor> enabledDoctors = doctorMapper.selectList(wrapper);
		Map<Long, Long> result = new HashMap<>();
		for (Doctor doctor : enabledDoctors) {
			if (doctor.getDeptId() != null) {
				result.merge(doctor.getDeptId(), 1L, Long::sum);
			}
		}
		return result;
	}

	private LambdaQueryWrapper<Doctor> buildListWrapper(Long departmentId, String keyword,
			Integer status, String title) {
		LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
		if (departmentId != null) {
			// 查找该科室及其所有子科室的ID
			List<Long> deptIds = findDeptAndChildIds(departmentId);
			if (deptIds.size() == 1) {
				wrapper.eq(Doctor::getDeptId, departmentId);
			} else {
				wrapper.in(Doctor::getDeptId, deptIds);
			}
		}
		if (status != null) {
			wrapper.eq(Doctor::getStatus, status);
		}
		if (hasText(keyword)) {
			wrapper.like(Doctor::getTitle, keyword).or().like(Doctor::getSpecialty, keyword);
		}
		if (hasText(title)) {
			wrapper.like(Doctor::getTitle, title);
		}
		wrapper.orderByDesc(Doctor::getCreatedAt);
		return wrapper;
	}

	/** 查找科室及其所有子科室的ID列表 */
	private List<Long> findDeptAndChildIds(Long deptId) {
		List<Long> result = new ArrayList<>();
		result.add(deptId);
		try {
			List<DepartmentResponse> allDepts = departmentQueryService.listDepartments(null, null);
			Map<Long, List<Long>> childrenMap = new HashMap<>();
			for (DepartmentResponse d : allDepts) {
				if (d.getParentId() != null) {
					childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getId());
				}
			}
			// 递归收集子科室
			collectChildIds(deptId, childrenMap, result);
		} catch (Exception ignored) {
		}
		return result;
	}

	private void collectChildIds(Long parentId, Map<Long, List<Long>> childrenMap, List<Long> result) {
		List<Long> children = childrenMap.get(parentId);
		if (children != null) {
			for (Long childId : children) {
				result.add(childId);
				collectChildIds(childId, childrenMap, result);
			}
		}
	}

	private static boolean hasText(String s) {
		return s != null && !s.isBlank();
	}

	/** 将数据库英文 doctor_type 转为中文展示。 */
	private static String doctorTypeToChinese(String type) {
		if (type == null) return null;
		return switch (type) {
			case "OUTPATIENT" -> "门诊医生";
			case "LAB" -> "检验医生";
			case "EXAM" -> "检查医生";
			case "PHARMACY" -> "药房医生";
			case "REGISTRATION" -> "挂号医生";
			default -> type;
		};
	}
}
