package com.home.infrastructure.batch.tradealarm.exception;

public class NonRetryableMailException extends RuntimeException {
	public NonRetryableMailException(String message, Throwable cause) {
		super(message, cause);
	}
}
