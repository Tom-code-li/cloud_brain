package com.neuCloudBrainMedical.admin.service.doctor;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.PageResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 医生查询服务（读操作）。
 * 遵循 Interface Segregation：与 Command 服务分离。
 */
public interface IDoctorQueryService {

	PageResponse<DoctorResponse> listDoctors(Long departmentId, String keyword,
	                                         Integer status, String title,
	                                         int pageNum, int pageSize);

	DoctorResponse getDoctorDetail(Long id);

	List<DoctorResponse> exportDoctors(Long departmentId);

	/** 排班管理页面的医生下拉列表：只返回启用状态。 */
	List<DoctorOptionDTO> listEnabledDoctors(Long departmentId);

	/** 获取所有可用的医生权限角色（供前端下拉选择）。 */
	List<DoctorRoleOption> listDoctorRoles();

	/**
	 * 批量根据 ID 查询医生信息（返回 doctorId -> DoctorInfo 的 Map）。
	 * 供跨模块 Service 调用，避免直接依赖 DoctorMapper。
	 */
	Map<Long, DoctorInfo> findDoctorInfoByIds(Set<Long> doctorIds);

	/**
	 * 查询指定科室下状态为"启用"的医生，返回带用户姓名的 DoctorInfo 列表。
	 * 供 AI 排班建议等跨模块功能调用。
	 */
	List<DoctorInfo> listEnabledDoctorInfosByDepartment(Long departmentId);

	/** 统计指定科室下的在职医生数量（用于删除科室前检查）。 */
	long countActiveDoctorsByDepartment(Long deptId);

	/** 返回各科室的启用医生统计（deptId -> 医生数量，用于仪表盘）。 */
	Map<Long, Long> getActiveDoctorCountByDepartmentMap();
}