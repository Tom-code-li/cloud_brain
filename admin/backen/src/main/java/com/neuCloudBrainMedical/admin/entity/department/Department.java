package com.neuCloudBrainMedical.admin.entity.department;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("department")
public class Department {
	public static final int STATUS_ENABLED = 1;
	public static final int STATUS_DISABLED = 0;

	@TableId(value = "dept_id", type = IdType.AUTO)
	private Long deptId;
	private Long parentId;
	private String deptCode;
	private String deptName;
	private String deptType;
	private String floor;
	private String phone;
	private String location;
	private String description;
	private Integer status;
	private Integer sortOrder;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Long getDeptId() { return deptId; }
	public void setDeptId(Long v) { this.deptId = v; }
	public Long getParentId() { return parentId; }
	public void setParentId(Long v) { this.parentId = v; }
	public String getDeptCode() { return deptCode; }
	public void setDeptCode(String v) { this.deptCode = v; }
	public String getDeptName() { return deptName; }
	public void setDeptName(String v) { this.deptName = v; }
	public String getDeptType() { return deptType; }
	public void setDeptType(String v) { this.deptType = v; }
	public String getFloor() { return floor; }
	public void setFloor(String v) { this.floor = v; }
	public String getPhone() { return phone; }
	public void setPhone(String v) { this.phone = v; }
	public String getLocation() { return location; }
	public void setLocation(String v) { this.location = v; }
	public String getDescription() { return description; }
	public void setDescription(String v) { this.description = v; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer v) { this.status = v; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer v) { this.sortOrder = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}