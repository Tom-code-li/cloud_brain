package com.neuCloudBrainMedical.admin.dto.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ScheduleBatchCreateRequest {

	@Valid
	@NotEmpty(message = "schedules is required")
	private List<ScheduleCreateRequest> schedules;

	public List<ScheduleCreateRequest> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<ScheduleCreateRequest> schedules) {
		this.schedules = schedules;
	}
}




