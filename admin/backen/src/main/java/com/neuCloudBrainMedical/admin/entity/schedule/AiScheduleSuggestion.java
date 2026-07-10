package com.neuCloudBrainMedical.admin.entity.schedule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("ai_schedule_suggestion")
public class AiScheduleSuggestion {

	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_ACCEPTED = "ACCEPTED";
	public static final String STATUS_REJECTED = "REJECTED";

	@TableId(value = "suggestion_id", type = IdType.AUTO)
	private Long suggestionId;
	private Long doctorId;
	private Long deptId;
	private LocalDate workDate;
	private String timePeriod;
	private Integer suggestedQuota;
	private String suggestionReason;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime confirmedAt;

	public Long getSuggestionId() { return suggestionId; }
	public void setSuggestionId(Long v) { this.suggestionId = v; }
	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long v) { this.doctorId = v; }
	public Long getDeptId() { return deptId; }
	public void setDeptId(Long v) { this.deptId = v; }
	public LocalDate getWorkDate() { return workDate; }
	public void setWorkDate(LocalDate v) { this.workDate = v; }
	public String getTimePeriod() { return timePeriod; }
	public void setTimePeriod(String v) { this.timePeriod = v; }
	public Integer getSuggestedQuota() { return suggestedQuota; }
	public void setSuggestedQuota(Integer v) { this.suggestedQuota = v; }
	public String getSuggestionReason() { return suggestionReason; }
	public void setSuggestionReason(String v) { this.suggestionReason = v; }
	public String getStatus() { return status; }
	public void setStatus(String v) { this.status = v; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
	public LocalDateTime getConfirmedAt() { return confirmedAt; }
	public void setConfirmedAt(LocalDateTime v) { this.confirmedAt = v; }
}