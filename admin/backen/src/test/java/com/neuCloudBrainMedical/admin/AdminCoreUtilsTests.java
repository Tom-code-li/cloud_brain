package com.neuCloudBrainMedical.admin;

import com.neuCloudBrainMedical.admin.exception.AIServiceException;
import com.neuCloudBrainMedical.admin.exception.BusinessException;
import com.neuCloudBrainMedical.admin.security.JwtTokenProvider;
import com.neuCloudBrainMedical.admin.util.Result;
import com.neuCloudBrainMedical.admin.util.ScheduleTimeSlotUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminCoreUtilsTests {

	private static final String SECRET = "0123456789abcdef0123456789abcdef";

	@Test
	void resultShouldWrapSuccessAndErrorAndExposeMutableFields() {
		Result<Void> empty = Result.success();
		assertEquals(200, empty.getCode());
		assertEquals("success", empty.getMessage());
		assertNull(empty.getData());

		Result<String> data = Result.success("ok");
		assertEquals("ok", data.getData());

		Result<Object> error = Result.error(403, "denied");
		assertEquals(403, error.getCode());
		assertEquals("denied", error.getMessage());
		assertNull(error.getData());

		error.setCode(500);
		error.setMessage("boom");
		error.setData("detail");
		assertEquals(500, error.getCode());
		assertEquals("boom", error.getMessage());
		assertEquals("detail", error.getData());
	}

	@Test
	void businessExceptionShouldExposeCodeAndMessage() {
		BusinessException exception = new BusinessException(409, "conflict");
		assertEquals(409, exception.getCode());
		assertEquals("conflict", exception.getMessage());
	}

	@Test
	void aiServiceExceptionShouldUse503Code() {
		AIServiceException exception = new AIServiceException("ai down");
		assertEquals(503, exception.getCode());
		assertEquals("ai down", exception.getMessage());
	}

	@Test
	void scheduleTimeSlotUtilsShouldNormalizeAndDeriveDefaults() {
		String morning = ScheduleTimeSlotUtils.normalize("MORNING");
		String afternoon = ScheduleTimeSlotUtils.normalize("AFTERNOON");
		String evening = ScheduleTimeSlotUtils.normalize("EVENING");

		assertEquals(morning, ScheduleTimeSlotUtils.displayName("MORNING"));
		assertEquals(LocalTime.of(8, 0), ScheduleTimeSlotUtils.defaultStartTime(morning));
		assertEquals(LocalTime.of(14, 0), ScheduleTimeSlotUtils.defaultStartTime(afternoon));
		assertEquals(LocalTime.of(18, 0), ScheduleTimeSlotUtils.defaultStartTime(evening));
		assertEquals(LocalTime.of(12, 0), ScheduleTimeSlotUtils.defaultEndTime(morning));
		assertEquals(LocalTime.of(17, 0), ScheduleTimeSlotUtils.defaultEndTime(afternoon));
		assertEquals(LocalTime.of(21, 0), ScheduleTimeSlotUtils.defaultEndTime(evening));
		assertEquals(new BigDecimal("15.00"), ScheduleTimeSlotUtils.defaultFeeByTitle(null));
		assertEquals(new BigDecimal("15.00"), ScheduleTimeSlotUtils.defaultFeeByTitle("UNKNOWN"));
	}

	@Test
	void scheduleTimeSlotUtilsShouldReportConflictsAndRejectInvalidValues() {
		assertTrue(ScheduleTimeSlotUtils.conflictValues("MORNING").contains("MORNING"));
		assertTrue(ScheduleTimeSlotUtils.conflictValues("AFTERNOON").contains("AFTERNOON"));
		assertTrue(ScheduleTimeSlotUtils.conflictValues("EVENING").contains("EVENING"));
		assertThrows(BusinessException.class, () -> ScheduleTimeSlotUtils.normalize("INVALID"));
		assertThrows(BusinessException.class, () -> ScheduleTimeSlotUtils.normalize(""));
	}

	@Test
	void jwtTokenProviderShouldGenerateAndValidateToken() {
		JwtTokenProvider provider = new JwtTokenProvider();
		ReflectionTestUtils.setField(provider, "secret", SECRET);
		ReflectionTestUtils.setField(provider, "expiration", 60_000L);

		String token = provider.generateToken(7L, "alice", "ADMIN");
		assertTrue(provider.validateToken(token));
		assertEquals(7L, provider.getUserIdFromToken(token));
		assertEquals("alice", provider.getUsernameFromToken(token));
	}

	@Test
	void jwtTokenProviderShouldRejectInvalidToken() {
		JwtTokenProvider provider = new JwtTokenProvider();
		ReflectionTestUtils.setField(provider, "secret", SECRET);
		ReflectionTestUtils.setField(provider, "expiration", 60_000L);

		String anotherSecret = "fedcba9876543210fedcba9876543210";
		SecretKey key = Keys.hmacShaKeyFor(anotherSecret.getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder()
				.subject("bob")
				.claim("userId", 9L)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();

		assertFalse(provider.validateToken(token));
	}
}
