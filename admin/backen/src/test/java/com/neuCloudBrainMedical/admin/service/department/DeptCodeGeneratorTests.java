package com.neuCloudBrainMedical.admin.service.department;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuCloudBrainMedical.admin.service.department.impl.DeptCodeGeneratorImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeptCodeGeneratorTests {

	@Test
	void generateCodeShouldFallbackWithoutApiKey() {
		DeptCodeGeneratorImpl generator = new DeptCodeGeneratorImpl(new RestTemplate(), new ObjectMapper());
		ReflectionTestUtils.setField(generator, "apiKey", "");

		String code = generator.generateCode("Inner Medicine");
		assertNotNull(code);
		assertFalse(code.isBlank());
	}

	@Test
	void generateCodeShouldFallbackForBlankName() {
		DeptCodeGeneratorImpl generator = new DeptCodeGeneratorImpl(new RestTemplate(), new ObjectMapper());
		ReflectionTestUtils.setField(generator, "apiKey", "");

		String code = generator.generateCode(" ");
		assertNotNull(code);
		assertFalse(code.isBlank());
	}

	@Test
	void generateCodeShouldUseAiResponseWhenAvailable() {
		RestTemplate restTemplate = mock(RestTemplate.class);
		DeptCodeGeneratorImpl generator = new DeptCodeGeneratorImpl(restTemplate, new ObjectMapper());
		ReflectionTestUtils.setField(generator, "apiKey", "key");
		ReflectionTestUtils.setField(generator, "baseUrl", "https://example.test/");
		ReflectionTestUtils.setField(generator, "model", "model");
		when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
				.thenReturn("{\"choices\":[{\"message\":{\"content\":\"cardio\"}}]}");

		assertEquals("CARDIO", generator.generateCode("Cardiology"));
	}
}
