package com.neuCloudBrainMedical.admin.dto.schedule;

import java.time.LocalDate;
import java.util.List;

public class AIScheduleSuggestionResponse {

	private Long suggestionId;
	private Long departmentId;
	private LocalDate startDate;
	private LocalDate endDate;
	private String status;
	private List<SuggestionDetailResponse> details;

	public Long getSuggestionId() {
		return suggestionId;
	}

	public void setSuggestionId(Long suggestionId) {
		this.suggestionId = suggestionId;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<SuggestionDetailResponse> getDetails() {
		return details;
	}

	public void setDetails(List<SuggestionDetailResponse> details) {
		this.details = details;
	}
}




