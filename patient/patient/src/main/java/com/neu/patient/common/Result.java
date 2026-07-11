package com.neu.patient.common;

public class Result<T> {

	private boolean success;
	private String message;
	private T data;

	public static <T> Result<T> ok(T data) {
		return ok("操作成功", data);
	}

	public static <T> Result<T> ok(String message, T data) {
		Result<T> result = new Result<>();
		result.setSuccess(true);
		result.setMessage(message);
		result.setData(data);
		return result;
	}

	public static <T> Result<T> fail(String message) {
		Result<T> result = new Result<>();
		result.setSuccess(false);
		result.setMessage(message);
		return result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
