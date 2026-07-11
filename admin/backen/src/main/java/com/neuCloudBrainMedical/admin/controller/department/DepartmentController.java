package com.neuCloudBrainMedical.admin.controller.department;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentCreateRequest;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentUpdateRequest;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentCommandService;
import com.neuCloudBrainMedical.admin.service.department.IDepartmentQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 科室管理（资源路径：/api/admin/department）。
 * 控制器只做两件事：
 *   1) 将 HTTP 参数转发给 Service；
 *   2) 用 {@link Result} 包裹返回值，保持前后端契约一致。
 * 不包含任何业务判断，不做任何实体构造。
 */
@RestController
@RequestMapping("/api/admin/department")
public class DepartmentController {

	private final IDepartmentQueryService queryService;
	private final IDepartmentCommandService commandService;

	public DepartmentController(IDepartmentQueryService queryService,
			IDepartmentCommandService commandService) {
		this.queryService = queryService;
		this.commandService = commandService;
	}

	// ---------- 查询 ----------

	@GetMapping
	public Result<List<DepartmentResponse>> listDepartments(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer status) {
		return Result.success(queryService.listDepartments(keyword, status));
	}

	@GetMapping("/tree")
	public Result<List<DepartmentTreeNode>> getDepartmentTree() {
		return Result.success(queryService.getDepartmentTree());
	}

	@GetMapping("/{id}")
	public Result<DepartmentResponse> getDepartmentDetail(@PathVariable Long id) {
		return Result.success(queryService.getDepartmentDetail(id));
	}

	// ---------- 命令 ----------

	@PostMapping
	public Result<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
		return Result.success(commandService.createDepartment(request));
	}

	@PutMapping("/{id}")
	public Result<DepartmentResponse> updateDepartment(@PathVariable Long id,
			@RequestBody DepartmentUpdateRequest request) {
		return Result.success(commandService.updateDepartment(id, request));
	}

	@PostMapping("/{id}/toggle-status")
	public Result<Void> toggleStatus(@PathVariable Long id) {
		commandService.toggleStatus(id);
		return Result.success();
	}

	@DeleteMapping("/{id}")
	public Result<Void> deleteDepartment(@PathVariable Long id) {
		commandService.deleteDepartment(id);
		return Result.success();
	}
}




