package com.neuCloudBrainMedical.admin.entity.doctor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("doctor")
public class Doctor {
	public static final int STATUS_ENABLED = 1;
	public static final int STATUS_DISABLED = 0;

	@TableId(value = "doctor_id", type = IdType.AUTO)
	private Long doctorId;
	private Long userId;
	private Long deptId;
	private String doctorNo;
	private String doctorType;
	private String title;
	private String specialty;
	private String introduction;
	private Integer status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long v) { this.doctorId = v; }
	public Long getUserId() { return userId; }
	public void setUserId(Long v) { this.userId = v; }
	public Long getDeptId() { return deptId; }
	public void setDeptId(Long v) { this.deptId = v; }
	public String getDoctorNo() { return doctorNo; }
	public void setDoctorNo(String v) { this.doctorNo = v; }
	public String getDoctorType() { return doctorType; }
	public void setDoctorType(String v) { this.doctorType = v; }
	public String getTitle() { return title; }
	public void setTitle(String v) { this.title = v; }
	public String getSpecialty() { return specialty; }
	public void setSpecialty(String v) { this.specialty = v; }
	public String getIntroduction() { return introduction; }
	public void setIntroduction(String v) { this.introduction = v; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer v) { this.status = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}