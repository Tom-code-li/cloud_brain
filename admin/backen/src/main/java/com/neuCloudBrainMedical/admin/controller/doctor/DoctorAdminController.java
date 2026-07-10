package com.neuCloudBrainMedical.admin.controller.doctor;

import com.neuCloudBrainMedical.admin.dto.doctor.DoctorCreateRequest;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorDisableCheckResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorOptionDTO;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorResponse;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorRoleOption;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorUpdateRequest;
import com.neuCloudBrainMedical.admin.dto.PageResponse;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorCommandService;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生管理 Controller：路由前缀 /api/admin/doctor。
 * 读操作走 IDoctorQueryService；写操作走 IDoctorCommandService。
 * 同时在 /api/admin/doctors 暴露旧的医生下拉列表接口，供排班管理页面兼容调用。
 *
 * 设计要点（SOLID）：
 *   - 单一职责：本类只处理 HTTP 协议相关逻辑，业务实现下沉到 Service 层
 *   - 接口隔离：通过两个专用接口（Query / Command）注入，避免实现类依赖倒置膨胀
 *   - 依赖反转：由 Spring 提供构造参数注入，本类不直接依赖具体实现
 */
@RestController
@RequestMapping({"/api/admin/doctor", "/api/admin/doctors"})
public class DoctorAdminController {

	private final IDoctorQueryService doctorQueryService;
	private final IDoctorCommandService doctorCommandService;

	public DoctorAdminController(IDoctorQueryService doctorQueryService,
	                             IDoctorCommandService doctorCommandService) {
		this.doctorQueryService = doctorQueryService;
		this.doctorCommandService = doctorCommandService;
	}

	// ==================== 读操作 ====================

	@GetMapping("/roles")
	public Result<List<DoctorRoleOption>> listDoctorRoles() {
		return Result.success(doctorQueryService.listDoctorRoles());
	}

	@GetMapping("/list")
	public Result<PageResponse<DoctorResponse>> listDoctors(
			@RequestParam(required = false) Long departmentId,
			@RequestParam(required = false, defaultValue = "") String keyword,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) String title,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {
		return Result.success(doctorQueryService.listDoctors(departmentId, keyword, status, title, pageNum, pageSize));
	}

	@GetMapping("/{id}")
	public Result<DoctorResponse> getDoctorDetail(@PathVariable Long id) {
		return Result.success(doctorQueryService.getDoctorDetail(id));
	}

	@GetMapping("/export")
	public Result<List<DoctorResponse>> exportDoctors(@RequestParam(required = false) Long departmentId) {
		return Result.success(doctorQueryService.exportDoctors(departmentId));
	}

	/** 医生下拉列表（排班管理页面使用）：仅返回启用状态。 */
	@GetMapping
	public Result<List<DoctorOptionDTO>> listEnabledDoctors(
			@RequestParam(required = false) Long departmentId) {
		return Result.success(doctorQueryService.listEnabledDoctors(departmentId));
	}

	@GetMapping("/{id}/disable-check")
	public Result<DoctorDisableCheckResponse> checkBeforeDisable(@PathVariable Long id) {
		return Result.success(doctorCommandService.checkBeforeDisable(id));
	}

	// ==================== 写操作 ====================

	@PostMapping
	public Result<DoctorResponse> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
		return Result.success(doctorCommandService.createDoctor(request));
	}

	@PutMapping("/{id}")
	public Result<DoctorResponse> updateDoctor(@PathVariable Long id,
	                                           @Valid @RequestBody DoctorUpdateRequest request) {
		return Result.success(doctorCommandService.updateDoctor(id, request));
	}

	@PatchMapping("/{id}/toggle-status")
	public Result<DoctorResponse> toggleStatus(@PathVariable Long id,
	                                           @RequestParam(defaultValue = "false") boolean force) {
		return Result.success(doctorCommandService.toggleStatus(id, force));
	}

	@DeleteMapping("/{id}")
	public Result<Void> deleteDoctor(@PathVariable Long id) {
		doctorCommandService.deleteDoctor(id);
		return Result.success();
	}
}