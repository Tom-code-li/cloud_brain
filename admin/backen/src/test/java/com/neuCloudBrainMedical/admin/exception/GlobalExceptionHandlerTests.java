package com.neuCloudBrainMedical.admin.exception;

import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTests {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldHandleBusinessException() {
		Result<Object> result = handler.handleBusinessException(new BusinessException(409, "conflict"));
		assertEquals(409, result.getCode());
		assertEquals("conflict", result.getMessage());
	}

	@Test
	void shouldHandleValidationException() {
		MethodParameter methodParameter = mock(MethodParameter.class);
		BindingResult bindingResult = mock(BindingResult.class);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("request", "name", "required")));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

		Result<Object> result = handler.handleMethodArgumentNotValidException(exception);

		assertEquals(400, result.getCode());
		assertEquals("name: required", result.getMessage());
	}

	@Test
	void shouldHandleConstraintViolationException() {
		Result<Object> result = handler.handleConstraintViolationException(new ConstraintViolationException("bad", java.util.Set.of()));
		assertEquals(400, result.getCode());
		assertEquals("bad", result.getMessage());
	}

	@Test
	void shouldHandleGenericException() {
		Result<Object> result = handler.handleException(new RuntimeException("boom"));
		assertEquals(500, result.getCode());
	}
}
