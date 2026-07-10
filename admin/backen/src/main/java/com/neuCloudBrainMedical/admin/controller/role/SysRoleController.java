package com.neuCloudBrainMedical.admin.controller.role;

import com.neuCloudBrainMedical.admin.dto.role.SysRoleRequest;
import com.neuCloudBrainMedical.admin.dto.role.SysRoleResponse;
import com.neuCloudBrainMedical.admin.service.role.IRoleCommandService;
import com.neuCloudBrainMedical.admin.service.role.IRoleQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class SysRoleController {

	private final IRoleQueryService queryService;
	private final IRoleCommandService commandService;

	public SysRoleController(IRoleQueryService queryService, IRoleCommandService commandService) {
		this.queryService = queryService;
		this.commandService = commandService;
	}

	@GetMapping
	public Result<List<SysRoleResponse>> listRoles() {
		return Result.success(queryService.listRoles());
	}

	@GetMapping("/{id}")
	public Result<SysRoleResponse> getRoleDetail(@PathVariable Long id) {
		return Result.success(queryService.getRoleDetail(id));
	}

	@PostMapping
	public Result<SysRoleResponse> createRole(@Valid @RequestBody SysRoleRequest request) {
		return Result.success(commandService.createRole(request));
	}

	@PutMapping("/{id}")
	public Result<SysRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody SysRoleRequest request) {
		return Result.success(commandService.updateRole(id, request));
	}

	@DeleteMapping("/{id}")
	public Result<Void> deleteRole(@PathVariable Long id) {
		commandService.deleteRole(id);
		return Result.success(null);
	}

	@PatchMapping("/{id}/toggle-status")
	public Result<Void> toggleStatus(@PathVariable Long id) {
		commandService.toggleStatus(id);
		return Result.success(null);
	}
}