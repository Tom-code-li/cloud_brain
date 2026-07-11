package com.neuCloudBrainMedical.admin.service.department.converter;

import com.neuCloudBrainMedical.admin.dto.department.DepartmentResponse;
import com.neuCloudBrainMedical.admin.dto.department.DepartmentTreeNode;
import com.neuCloudBrainMedical.admin.entity.department.Department;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 科室 Entity -> DTO 装配组件。
 * 单一职责：只负责 Department <-> DepartmentResponse / DepartmentTreeNode 转换。
 */
@Component
public class DepartmentResponseAssembler {

	public List<DepartmentResponse> toResponseList(List<Department> departments) {
		if (departments == null) return Collections.emptyList();
		return departments.stream().map(this::toResponse).toList();
	}

	public DepartmentResponse toResponse(Department d) {
		if (d == null) return null;
		DepartmentResponse resp = new DepartmentResponse();
		resp.setId(d.getDeptId());
		resp.setParentId(d.getParentId());
		resp.setName(d.getDeptName());
		resp.setCode(d.getDeptCode());
		resp.setDepartmentType(deptTypeToChinese(d.getDeptType()));
		resp.setFloor(d.getFloor());
		resp.setPhone(d.getPhone());
		resp.setDescription(d.getDescription());
		resp.setStatus(d.getStatus());
		resp.setSortOrder(d.getSortOrder());
		return resp;
	}

	public List<DepartmentTreeNode> toTreeNodes(List<Department> departments) {
		if (departments == null || departments.isEmpty()) return Collections.emptyList();
		Map<Long, DepartmentTreeNode> map = new HashMap<>();
		for (Department d : departments) {
			map.put(d.getDeptId(), new DepartmentTreeNode(d.getDeptId(), d.getDeptCode(), d.getDeptName(), deptTypeToChinese(d.getDeptType())));
		}
		List<DepartmentTreeNode> roots = new ArrayList<>();
		for (Department d : departments) {
			DepartmentTreeNode self = map.get(d.getDeptId());
			if (d.getParentId() != null && map.containsKey(d.getParentId())) {
				DepartmentTreeNode parent = map.get(d.getParentId());
				if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
				parent.getChildren().add(self);
			} else {
				roots.add(self);
			}
		}
		return roots;
	}

	public List<DepartmentResponse> buildAncestorChain(Long id, List<Department> departments) {
		if (id == null || departments == null || departments.isEmpty()) return Collections.emptyList();
		Map<Long, Department> byId = new HashMap<>();
		for (Department d : departments) byId.put(d.getDeptId(), d);
		List<DepartmentResponse> chain = new ArrayList<>();
		Department current = byId.get(id);
		while (current != null) {
			chain.add(0, toResponse(current));
			current = current.getParentId() != null ? byId.get(current.getParentId()) : null;
		}
		return chain;
	}

	private static String deptTypeToChinese(String type) {
		if (type == null) return null;
		return switch (type) {
			case "OUTPATIENT" -> "门诊";
			case "LAB" -> "检验";
			case "EXAM" -> "检查";
			case "PHARMACY" -> "药房";
			case "BILLING" -> "收费";
			default -> type;
		};
	}
}