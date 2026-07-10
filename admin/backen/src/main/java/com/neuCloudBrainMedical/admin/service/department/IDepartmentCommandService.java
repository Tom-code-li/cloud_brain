package com.neuCloudBrainMedical.admin.service.department;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;

/**
 * 科室命令接口（写操作）。
 * 与 {@link IDepartmentQueryService} 分离，遵循 ISP。
 */
public interface IDepartmentCommandService {

	DepartmentResponse createDepartment(DepartmentCreateRequest request);

	DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);

	/** 切换启用/停用状态（避免 0/1 硬编码在前端）。 */
	void toggleStatus(Long id);

	/**
	 * 删除科室。
	 * 如科室下存在子科室 / 关联医生 / 关联排班记录，则禁止删除并抛
	 * {@link com.neuCloudBrainMedical.admin.exception.BusinessException}。
	 */
	void deleteDepartment(Long id);
}




