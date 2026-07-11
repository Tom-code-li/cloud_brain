package com.neuCloudBrainMedical.admin.util;

import com.neuCloudBrainMedical.admin.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * 排班相关工具方法（时段归一化、默认时间、默认挂号费）。
 * <p>
 * 所有时段在数据库中以中文值存储：上午 / 下午 / 夜间。
 * 本类兼容前端可能传来的旧英文值（MORNING / AFTERNOON / EVENING）。
 */
public final class ScheduleTimeSlotUtils {

	private ScheduleTimeSlotUtils() {
	}

	// ==================== 时段归一化 ====================

	/**
	 * 将时段值归一化为数据库标准中文值（上午/下午/夜间）。
	 * 同时兼容前端传来的旧英文值。
	 */
	public static String normalize(String timeSlot) {
		if (timeSlot == null || timeSlot.isBlank()) {
			throw new BusinessException(400, "请选择排班时段");
		}
		String value = timeSlot.trim();
		return switch (value) {
			case "上午", "MORNING" -> "上午";
			case "下午", "AFTERNOON" -> "下午";
			case "夜间", "晚间", "晚上", "EVENING" -> "夜间";
			default -> throw new BusinessException(400, "排班时段只能是上午、下午或夜间");
		};
	}

	/**
	 * 返回与指定时段冲突的所有可能值（用于数据库查询）。
	 * 包含中文值和旧英文值，确保历史数据也能匹配。
	 */
	public static List<String> conflictValues(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> List.of("上午", "MORNING");
			case "下午" -> List.of("下午", "AFTERNOON");
			case "夜间" -> List.of("夜间", "EVENING", "晚间", "晚上");
			default -> List.of(timeSlot);
		};
	}

	/** 将时段值转为显示用的中文名。 */
	public static String displayName(String timeSlot) {
		return normalize(timeSlot);
	}

	// ==================== 默认时间段 ====================

	/** 根据时段返回默认开始时间。 */
	public static LocalTime defaultStartTime(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> LocalTime.of(8, 0);
			case "下午" -> LocalTime.of(14, 0);
			case "夜间" -> LocalTime.of(18, 0);
			default -> LocalTime.of(8, 0);
		};
	}

	/** 根据时段返回默认结束时间。 */
	public static LocalTime defaultEndTime(String timeSlot) {
		return switch (normalize(timeSlot)) {
			case "上午" -> LocalTime.of(12, 0);
			case "下午" -> LocalTime.of(17, 0);
			case "夜间" -> LocalTime.of(21, 0);
			default -> LocalTime.of(12, 0);
		};
	}

	// ==================== 默认挂号费 ====================

	private static final BigDecimal FEE_DEFAULT    = new BigDecimal("15.00");
	private static final BigDecimal FEE_CHIEF      = new BigDecimal("50.00");
	private static final BigDecimal FEE_VICE_CHIEF = new BigDecimal("25.00");
	private static final BigDecimal FEE_ATTENDING  = new BigDecimal("15.00");
	private static final BigDecimal FEE_RESIDENT   = new BigDecimal("10.00");

	/** 根据医生职称返回默认挂号费。 */
	public static BigDecimal defaultFeeByTitle(String title) {
		if (title == null) return FEE_DEFAULT;
		return switch (title) {
			case "主任医师"   -> FEE_CHIEF;
			case "副主任医师" -> FEE_VICE_CHIEF;
			case "主治医师"   -> FEE_ATTENDING;
			case "住院医师"   -> FEE_RESIDENT;
			default           -> FEE_DEFAULT;
		};
	}
}