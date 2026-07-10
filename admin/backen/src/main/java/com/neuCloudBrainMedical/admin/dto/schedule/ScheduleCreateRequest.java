package com.neuCloudBrainMedical.admin.dto.schedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ScheduleCreateRequest {

	@NotNull(message = "doctorId is required")
	private Long doctorId;

	@NotNull(message = "departmentId is required")
	private Long departmentId;

	@NotNull(message = "scheduleDate is required")
	private LocalDate scheduleDate;

	@NotNull(message = "timeSlot is required")
	private String timeSlot;

	@NotNull(message = "maxAppointments is required")
	private Integer maxAppointments;

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(LocalDate scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public Integer getMaxAppointments() {
		return maxAppointments;
	}

	public void setMaxAppointments(Integer maxAppointments) {
		this.maxAppointments = maxAppointments;
	}
}




