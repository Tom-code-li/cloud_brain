package com.neuCloudBrainMedical.admin.entity.schedule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

/**
 * AI 排班建议明细。
 *
 * <p>每条明细代表一个具体的排班建议：某个医生在某一天某个时段的排班建议，
 * 包含最大接诊量和建议理由。状态为 PENDING / ACCEPTED / REJECTED。</p>
 */
@TableName("ai_schedule_suggestion_detail")
public class AiScheduleSuggestionDetail {

	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_ACCEPTED = "ACCEPTED";
	public static final String STATUS_REJECTED = "REJECTED";

	@TableId(value = "detail_id", type = IdType.AUTO)
	private Long detailId;
	private Long suggestionId;
	private Long doctorId;
	private String doctorName;
	private LocalDate scheduleDate;
	private String timeSlot;
	private Integer maxAppointments;
	private String reason;
	private String status;

	public Long getDetailId() { return detailId; }
	public void setDetailId(Long v) { this.detailId = v; }
	public Long getSuggestionId() { return suggestionId; }
	public void setSuggestionId(Long v) { this.suggestionId = v; }
	public Long getDoctorId() { return doctorId; }
	public void setDoctorId(Long v) { this.doctorId = v; }
	public String getDoctorName() { return doctorName; }
	public void setDoctorName(String v) { this.doctorName = v; }
	public LocalDate getScheduleDate() { return scheduleDate; }
	public void setScheduleDate(LocalDate v) { this.scheduleDate = v; }
	public String getTimeSlot() { return timeSlot; }
	public void setTimeSlot(String v) { this.timeSlot = v; }
	public Integer getMaxAppointments() { return maxAppointments; }
	public void setMaxAppointments(Integer v) { this.maxAppointments = v; }
	public String getReason() { return reason; }
	public void setReason(String v) { this.reason = v; }
	public String getStatus() { return status; }
	public void setStatus(String v) { this.status = v; }
}