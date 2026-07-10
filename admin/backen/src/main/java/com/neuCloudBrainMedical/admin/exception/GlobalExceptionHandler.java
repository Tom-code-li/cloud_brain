package com.neuCloudBrainMedical.admin.exception;

import com.neuCloudBrainMedical.admin.util.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public Result<Object> handleBusinessException(BusinessException exception) {
		return Result.error(exception.getCode(), exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("参数校验失败");
		return Result.error(400, message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public Result<Object> handleConstraintViolationException(ConstraintViolationException exception) {
		return Result.error(400, exception.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<Object> handleException(Exception exception) {
		log.error("未捕获异常：{}", exception.getMessage(), exception);
		return Result.error(500, "服务器内部错误：" + exception.getMessage());
	}
}
