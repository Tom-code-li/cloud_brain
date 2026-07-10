package com.neuCloudBrainMedical.admin.service.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.dto.doctor.DoctorInfo;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.service.schedule.impl.DefaultAISchedulingClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAISchedulingClientTests {

	@Test
	void shouldRejectMissingApiKey() {
		DefaultAISchedulingClient client = new DefaultAISchedulingClient(new RestTemplate(), new ObjectMapper());
		ReflectionTestUtils.setField(client, "apiKey", "");

		assertThrows(BusinessException.class, () -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));
	}

	@Test
	void shouldExtractJsonArrayFromChatResponse() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DefaultAISchedulingClient client = new DefaultAISchedulingClient(restTemplate, new ObjectMapper());
		ReflectionTestUtils.setField(client, "apiKey", "key");
		ReflectionTestUtils.setField(client, "baseUrl", "https://example.test/");
		ReflectionTestUtils.setField(client, "model", "model");
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
				.thenReturn("{\"choices\":[{\"message\":{\"content\":\"text [{\\\"doctorId\\\":1}] end\"}}]}");

		String result = client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx");

		assertEquals("[{\"doctorId\":1}]", result);
	}

	@Test
	void shouldRejectInvalidChatResponse() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DefaultAISchedulingClient client = new DefaultAISchedulingClient(restTemplate, new ObjectMapper());
		ReflectionTestUtils.setField(client, "apiKey", "key");
		ReflectionTestUtils.setField(client, "baseUrl", "https://example.test/");
		ReflectionTestUtils.setField(client, "model", "model");
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("{}");

		assertThrows(BusinessException.class, () -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));
	}

	@Test
	void shouldRejectBlankAndMalformedResponses() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DefaultAISchedulingClient client = configuredClient(restTemplate);
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(" ");

		assertThrows(BusinessException.class, () -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));

		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("not-json");
		assertThrows(BusinessException.class, () -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));
	}

	@Test
	void shouldRejectTextualContentWithoutJsonArray() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DefaultAISchedulingClient client = configuredClient(restTemplate);
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
				.thenReturn("{\"choices\":[{\"message\":{\"content\":\"no array here\"}}]}");

		assertThrows(BusinessException.class, () -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));
	}

	@Test
	void shouldWrapRestTemplateFailure() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DefaultAISchedulingClient client = configuredClient(restTemplate);
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("network"));

		BusinessException exception = assertThrows(BusinessException.class,
				() -> client.requestSchedulingSuggestion(1L, LocalDate.now(), LocalDate.now(), List.of(doctor()), "ctx"));

		assertEquals(500, exception.getCode());
	}

	private DefaultAISchedulingClient configuredClient(RestTemplate restTemplate) {
		DefaultAISchedulingClient client = new DefaultAISchedulingClient(restTemplate, new ObjectMapper());
		ReflectionTestUtils.setField(client, "apiKey", "key");
		ReflectionTestUtils.setField(client, "baseUrl", "https://example.test///");
		ReflectionTestUtils.setField(client, "model", "model");
		return client;
	}

	private DoctorInfo doctor() {
		DoctorInfo doctor = new DoctorInfo();
		doctor.setDoctorId(1L);
		doctor.setDoctorName("Dr A");
		doctor.setTitle("Chief");
		doctor.setSpecialty("Heart");
		return doctor;
	}
}
