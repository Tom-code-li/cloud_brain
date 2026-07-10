package com.neuCloudBrainMedical.admin.dto.department;

import java.util.List;

/** 科室树形节点（用于 el-tree，包含 id / code / name / children）。 */
public class DepartmentTreeNode {

	private Long id;
	private String code;
	private String name;
	private String departmentType;
	private List<DepartmentTreeNode> children;

	public DepartmentTreeNode() {}

	public DepartmentTreeNode(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public DepartmentTreeNode(Long id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public DepartmentTreeNode(Long id, String code, String name, String departmentType) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.departmentType = departmentType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepartmentType() {
		return departmentType;
	}

	public void setDepartmentType(String departmentType) {
		this.departmentType = departmentType;
	}

	public List<DepartmentTreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<DepartmentTreeNode> children) {
		this.children = children;
	}
}