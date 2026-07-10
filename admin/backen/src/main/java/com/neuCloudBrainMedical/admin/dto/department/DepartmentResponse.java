package com.neuCloudBrainMedical.admin.dto.department;

import java.util.List;

/** 科室响应（列表/详情通用，附带 children 用于树形结构展示）。 */
public class DepartmentResponse {

	private Long id;
	private Long parentId;
	private String name;
	private String code;
	private String departmentType;
	private String floor;
	private String phone;
	private String description;
	private Integer status;
	private Integer sortOrder;
	private List<DepartmentResponse> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDepartmentType() {
		return departmentType;
	}

	public void setDepartmentType(String departmentType) {
		this.departmentType = departmentType;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<DepartmentResponse> getChildren() {
		return children;
	}

	public void setChildren(List<DepartmentResponse> children) {
		this.children = children;
	}
}



