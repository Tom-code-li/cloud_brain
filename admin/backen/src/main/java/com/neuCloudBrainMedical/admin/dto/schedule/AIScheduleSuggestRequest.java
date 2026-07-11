package com.neuCloudBrainMedical.admin.dto.schedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AIScheduleSuggestRequest {

	@NotNull(message = "departmentId is required")
	private Long departmentId;

	@NotNull(message = "startDate is required")
	private LocalDate startDate;

	@NotNull(message = "endDate is required")
	private LocalDate endDate;

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
}




