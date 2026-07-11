package com.neuCloudBrainMedical.admin.entity.schedule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("doctor_schedule")
public class DoctorSchedule {

	public static final String STATUS_ACTIVE = "可预约";
	public static final String STATUS_FULL = "约满";
	public static final String STATUS_CANCELLED = "已停诊";
	public static final String STATUS_EXPIRED = "已过期";

	@TableId(value = "schedule_id", type = IdType.AUTO)
	private Long scheduleId;
	private Long doctorId;
	private Long deptId;
	private LocalDate workDate;
	private String timePeriod;
	private LocalTime startTime;
	private LocalTime endTime;
	private Integer totalQuota;
	private Integer remainQuota;
	private BigDecimal registrationFee;
	private String status;
	private String source;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Long getScheduleId() { return scheduleId; }
	public void setScheduleId(Long v) { this.scheduleId = v; }
	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long v) { this.doctorId = v; }
	public Long getDeptId() { return deptId; }
	public void setDeptId(Long v) { this.deptId = v; }
	public LocalDate getWorkDate() { return workDate; }
	public void setWorkDate(LocalDate v) { this.workDate = v; }
	public String getTimePeriod() { return timePeriod; }
	public void setTimePeriod(String v) { this.timePeriod = v; }
	public LocalTime getStartTime() { return startTime; }
	public void setStartTime(LocalTime v) { this.startTime = v; }
	public LocalTime getEndTime() { return endTime; }
	public void setEndTime(LocalTime v) { this.endTime = v; }
	public Integer getTotalQuota() { return totalQuota; }
	public void setTotalQuota(Integer v) { this.totalQuota = v; }
	public Integer getRemainQuota() { return remainQuota; }
	public void setRemainQuota(Integer v) { this.remainQuota = v; }
	public BigDecimal getRegistrationFee() { return registrationFee; }
	public void setRegistrationFee(BigDecimal v) { this.registrationFee = v; }
	public String getStatus() { return status; }
	public void setStatus(String v) { this.status = v; }
	public String getSource() { return source; }
	public void setSource(String v) { this.source = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}