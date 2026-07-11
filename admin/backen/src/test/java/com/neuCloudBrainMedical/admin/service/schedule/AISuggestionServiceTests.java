package com.neuCloudBrainMedical.admin.service.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestRequest;
import com.neuCloudBrainMedical.admin.dto.schedule.AIScheduleSuggestionResponse;
import com.neuCloudBrainMedical.admin.dto.schedule.ScheduleResponse;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestion;
import com.neuCloudBrainMedical.admin.entity.schedule.AiScheduleSuggestionDetail;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionDetailMapper;
import com.neuCloudBrainMedical.admin.mapper.admin.schedule.AiScheduleSuggestionMapper;
import com.neuCloudBrainMedical.admin.service.doctor.IDoctorQueryService;
import com.neuCloudBrainMedical.admin.service.schedule.impl.AISuggestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AISuggestionServiceTests {

	@Mock
	private AiScheduleSuggestionMapper suggestionMapper;
	@Mock
	private AiScheduleSuggestionDetailMapper detailMapper;
	@Mock
	private IDoctorQueryService doctorQueryService;
	@Mock
	private IScheduleCommandService scheduleCommandService;
	@Mock
	private IAISchedulingClient aiSchedulingClient;
	@Mock
	private IAISuggestionQueryService suggestionQueryService;

	private AISuggestionServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new AISuggestionServiceImpl(
				suggestionMapper,
				detailMapper,
				doctorQueryService,
				scheduleCommandService,
				aiSchedulingClient,
				suggestionQueryService,
				new ObjectMapper()
		);
	}

	@Test
	void generateSuggestionShouldFallbackWhenAiFails() {
		AIScheduleSuggestRequest request = request();
		when(doctorQueryService.listEnabledDoctorInfosByDepartment(10L)).thenReturn(List.of(doctor()));
		when(aiSchedulingClient.requestSchedulingSuggestion(anyLong(), any(), any(), any(), any())).thenThrow(new RuntimeException("down"));
		when(suggestionMapper.insert(any(AiScheduleSuggestion.class))).thenAnswer(invocation -> {
			AiScheduleSuggestion suggestion = invocation.getArgument(0);
			suggestion.setSuggestionId(1L);
			return 1;
		});
		when(suggestionQueryService.toResponse(any(), any())).thenReturn(new AIScheduleSuggestionResponse());

		assertNotNull(service.generateSuggestion(request));
		verify(detailMapper, atLeastOnce()).insert(any(AiScheduleSuggestionDetail.class));
	}

	@Test
	void generateSuggestionShouldPersistParsedAiDetails() {
		AIScheduleSuggestRequest request = request();
		when(doctorQueryService.listEnabledDoctorInfosByDepartment(10L)).thenReturn(List.of(doctor()));
		when(aiSchedulingClient.requestSchedulingSuggestion(anyLong(), any(), any(), any(), any()))
				.thenReturn("[{\"doctorId\":1,\"doctorName\":\"Dr A\",\"date\":\"" + LocalDate.now() + "\",\"timeSlot\":\"MORNING\",\"maxAppointments\":12,\"reason\":\"ok\"}]");
		when(suggestionMapper.insert(any(AiScheduleSuggestion.class))).thenAnswer(invocation -> {
			AiScheduleSuggestion suggestion = invocation.getArgument(0);
			suggestion.setSuggestionId(1L);
			return 1;
		});
		when(suggestionQueryService.toResponse(any(), any())).thenReturn(new AIScheduleSuggestionResponse());

		assertNotNull(service.generateSuggestion(request));
		verify(detailMapper).insert(any(AiScheduleSuggestionDetail.class));
	}

	@Test
	void generateSuggestionShouldFallbackWhenAiJsonCannotProduceDetails() {
		AIScheduleSuggestRequest request = request();
		when(doctorQueryService.listEnabledDoctorInfosByDepartment(10L)).thenReturn(List.of(doctor()));
		when(aiSchedulingClient.requestSchedulingSuggestion(anyLong(), any(), any(), any(), any())).thenReturn("{}");
		when(suggestionMapper.insert(any(AiScheduleSuggestion.class))).thenAnswer(invocation -> {
			AiScheduleSuggestion suggestion = invocation.getArgument(0);
			suggestion.setSuggestionId(suggestion.getSuggestionId() == null ? 1L : suggestion.getSuggestionId());
			return 1;
		});
		when(suggestionQueryService.toResponse(any(), any())).thenReturn(new AIScheduleSuggestionResponse());

		assertNotNull(service.generateSuggestion(request));
		verify(detailMapper).delete(any());
		verify(suggestionMapper).deleteById(1L);
		verify(detailMapper, atLeastOnce()).insert(any(AiScheduleSuggestionDetail.class));
	}

	@Test
	void generateSuggestionShouldRejectNoDoctorsOrInvalidDateRange() {
		AIScheduleSuggestRequest request = request();
		when(doctorQueryService.listEnabledDoctorInfosByDepartment(10L)).thenReturn(List.of());
		assertThrows(BusinessException.class, () -> service.generateSuggestion(request));

		request.setEndDate(request.getStartDate().minusDays(1));
		when(doctorQueryService.listEnabledDoctorInfosByDepartment(10L)).thenReturn(List.of(doctor()));
		assertThrows(BusinessException.class, () -> service.generateSuggestion(request));
	}

	@Test
	void acceptSuggestionDetailShouldCreateScheduleAndUpdateDetail() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail detail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectById(2L)).thenReturn(detail);
		when(scheduleCommandService.createScheduleWithSource(any(), any())).thenReturn(new ScheduleResponse());
		when(detailMapper.selectList(any())).thenReturn(List.of());

		assertNotNull(service.acceptSuggestionDetail(1L, 2L));
		verify(detailMapper).updateById(detail);
		verify(suggestionMapper).updateById(suggestion);
	}

	@Test
	void acceptSuggestionShouldAcceptAllPendingDetails() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail detail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectList(any())).thenReturn(List.of(detail));
		when(scheduleCommandService.createScheduleWithSource(any(), any())).thenReturn(new ScheduleResponse());

		List<ScheduleResponse> result = service.acceptSuggestion(1L);

		assertEquals(1, result.size());
		assertEquals(AiScheduleSuggestion.STATUS_ACCEPTED, suggestion.getStatus());
		verify(suggestionMapper).updateById(suggestion);
	}

	@Test
	void acceptSuggestionShouldRejectWhenNoPendingDetailsRemain() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectList(any())).thenReturn(List.of());

		assertThrows(BusinessException.class, () -> service.acceptSuggestion(1L));
	}

	@Test
	void rejectSuggestionShouldRejectPendingDetails() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail detail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectList(any())).thenReturn(List.of(detail));

		service.rejectSuggestion(1L);

		assertEquals(AiScheduleSuggestion.STATUS_REJECTED, suggestion.getStatus());
		verify(detailMapper).updateById(detail);
		verify(suggestionMapper).updateById(suggestion);
	}

	@Test
	void rejectSuggestionDetailShouldRejectSingleDetailAndCloseSuggestionWhenNoPendingDetailsRemain() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail detail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectById(2L)).thenReturn(detail);
		when(detailMapper.selectList(any())).thenReturn(List.of());

		service.rejectSuggestionDetail(1L, 2L);

		assertEquals(AiScheduleSuggestionDetail.STATUS_REJECTED, detail.getStatus());
		assertEquals(AiScheduleSuggestion.STATUS_REJECTED, suggestion.getStatus());
		verify(detailMapper).updateById(detail);
		verify(suggestionMapper).updateById(suggestion);
	}

	@Test
	void detailOperationsShouldKeepSuggestionOpenWhenPendingDetailsRemain() {
		AiScheduleSuggestion suggestion = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail detail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		AiScheduleSuggestionDetail remaining = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		remaining.setDetailId(3L);
		when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
		when(detailMapper.selectById(2L)).thenReturn(detail);
		when(detailMapper.selectList(any())).thenReturn(List.of(remaining));
		when(scheduleCommandService.createScheduleWithSource(any(), any())).thenReturn(new ScheduleResponse());

		assertNotNull(service.acceptSuggestionDetail(1L, 2L));
		assertEquals(AiScheduleSuggestion.STATUS_PENDING, suggestion.getStatus());
		verify(suggestionMapper, never()).updateById(suggestion);

		AiScheduleSuggestionDetail rejectDetail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		when(detailMapper.selectById(4L)).thenReturn(rejectDetail);
		service.rejectSuggestionDetail(1L, 4L);
		assertEquals(AiScheduleSuggestion.STATUS_PENDING, suggestion.getStatus());
	}

	@Test
	void shouldRejectInvalidSuggestionAndDetailStates() {
		assertThrows(BusinessException.class, () -> service.acceptSuggestion(null));

		AiScheduleSuggestion accepted = suggestion(AiScheduleSuggestion.STATUS_ACCEPTED);
		when(suggestionMapper.selectById(1L)).thenReturn(accepted);
		assertThrows(BusinessException.class, () -> service.rejectSuggestion(1L));

		AiScheduleSuggestion pending = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		AiScheduleSuggestionDetail wrongSuggestionDetail = detail(AiScheduleSuggestionDetail.STATUS_PENDING);
		wrongSuggestionDetail.setSuggestionId(99L);
		when(suggestionMapper.selectById(2L)).thenReturn(pending);
		when(detailMapper.selectById(2L)).thenReturn(wrongSuggestionDetail);
		assertThrows(BusinessException.class, () -> service.acceptSuggestionDetail(2L, 2L));
	}

	@Test
	void shouldRejectMissingAndAlreadyHandledDetails() {
		when(suggestionMapper.selectById(404L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> service.rejectSuggestion(404L));

		AiScheduleSuggestion pending = suggestion(AiScheduleSuggestion.STATUS_PENDING);
		when(suggestionMapper.selectById(1L)).thenReturn(pending);
		assertThrows(BusinessException.class, () -> service.acceptSuggestionDetail(1L, null));

		when(detailMapper.selectById(404L)).thenReturn(null);
		assertThrows(BusinessException.class, () -> service.rejectSuggestionDetail(1L, 404L));

		AiScheduleSuggestionDetail accepted = detail(AiScheduleSuggestionDetail.STATUS_ACCEPTED);
		when(detailMapper.selectById(2L)).thenReturn(accepted);
		assertThrows(BusinessException.class, () -> service.acceptSuggestionDetail(1L, 2L));
		assertThrows(BusinessException.class, () -> service.rejectSuggestionDetail(1L, 2L));
	}

	private AIScheduleSuggestRequest request() {
		AIScheduleSuggestRequest request = new AIScheduleSuggestRequest();
		request.setDepartmentId(10L);
		request.setStartDate(LocalDate.now());
		request.setEndDate(LocalDate.now());
		return request;
	}

	private DoctorInfo doctor() {
		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(1L);
		doctor.setDoctorName("Dr A");
		return doctor;
	}

	private AiScheduleSuggestion suggestion(String status) {
		AiScheduleSuggestion suggestion = new AiScheduleSuggestion();
		suggestion.setSuggestionId(1L);
		suggestion.setDeptId(10L);
		suggestion.setStatus(status);
		return suggestion;
	}

	private AiScheduleSuggestionDetail detail(String status) {
		AiScheduleSuggestionDetail detail = new AiScheduleSuggestionDetail();
		detail.setDetailId(2L);
		detail.setSuggestionId(1L);
		detail.setDoctorId(1L);
		detail.setScheduleDate(LocalDate.now());
		detail.setTimeSlot("MORNING");
		detail.setMaxAppointments(10);
		detail.setStatus(status);
		return detail;
	}
}
