package com.neuCloudBrainMedical.admin.service.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionDetailMapper;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionMapper;
import com.neuCloudBrainMedical.admin.service.schedule.impl.AISuggestionQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AISuggestionQueryServiceTests {

	@Mock
	private AiScheduleSuggestionMapper suggestionMapper;
	@Mock
	private AiScheduleSuggestionDetailMapper detailMapper;

	private AISuggestionQueryServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new AISuggestionQueryServiceImpl(suggestionMapper, detailMapper);
	}

	@Test
	void shouldListSuggestionsAndDepartmentSuggestions() {
		AiScheduleSuggestion suggestion = suggestion();
		when(suggestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(suggestion));

		assertEquals(1, service.listSuggestions().size());
		assertEquals(1, service.listSuggestionsByDepartment(10L).size());
	}

	@Test
	void shouldReturnDetailAndDetails() {
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion());
		when(detailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(detail()));

		assertEquals(1L, service.getSuggestionDetail(1L).getSuggestionId());
		assertEquals(1, service.listDetailsBySuggestionId(1L).size());
	}

	@Test
	void shouldHandleEmptyAndMissingValues() {
		assertTrue(service.listDetailsBySuggestionId(null).isEmpty());
		assertThrows(BusinessException.class, () -> service.getSuggestionDetail(null));
		when(suggestionMapper.selectById(9L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> service.getSuggestionDetail(9L));
		when(suggestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
		assertTrue(service.listSuggestions().isEmpty());
	}

	@Test
	void shouldCountSuggestionsByTimeRange() {
		when(suggestionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
		assertEquals(3L, service.countSuggestionsByTimeRange(LocalDateTime.now().minusDays(1), LocalDateTime.now()));
	}

	private AiScheduleSuggestion suggestion() {
		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setSuggestionId(1L);
		suggestion.setDeptId(10L);
		suggestion.setWorkDate(LocalDate.now());
		suggestion.setStatus(AiScheduleSuggestion.STATUS_PENDING);
		return suggestion;
	}

	private AiScheduleSuggestionDetail detail() {
		AiScheduleSuggestionDetail detail = new AiScheduleSuggestionDetail();
		detail.setDetailId(2L);
		detail.setSuggestionId(1L);
		detail.setDoctorId(3L);
		detail.setDoctorName("Dr A");
		detail.setScheduleDate(LocalDate.now());
		detail.setTimeSlot("MORNING");
		detail.setMaxAppointments(10);
		detail.setReason("reason");
		detail.setStatus(AiScheduleSuggestionDetail.STATUS_PENDING);
		return detail;
	}
}
