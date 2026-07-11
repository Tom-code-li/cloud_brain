package com.neuCloudBrainMedical.admin.exception;

public class AIServiceException extends BusinessException {

	public AIServiceException(String message) {
		super(503, message);
	}
}
