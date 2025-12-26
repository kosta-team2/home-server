package com.home.infrastructure.batch.tradealarm.exception;

public class RetryableMailException extends RuntimeException {
	public RetryableMailException(String message, Throwable cause) {
		super(message, cause);
	}
}
