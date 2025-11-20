package com.home.global.exception;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * [Exception] @Valid, @RequestBody 검증 실패
	 * */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		return createProblemDetail(ex, ErrorCode.INVALID_INPUT);
	}

	/**
	 * [Exception] 예측하지 못한 모든 예외 처리
	 * */
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception ex) {
		return createProblemDetail(ex, ErrorCode.INTERNAL_ERROR);
	}

	private ProblemDetail createProblemDetail(Exception ex, ErrorCode errorCode) {
		ProblemDetail problemDetail = ProblemDetail.forStatus(errorCode.getHttpStatus());
		problemDetail.setType(URI.create("/docs/errors.html#" + errorCode.getTitle()));
		problemDetail.setTitle(errorCode.getTitle());
		problemDetail.setDetail(errorCode.getDetail());
		problemDetail.setProperty("exception", ex.getClass().getSimpleName());
		problemDetail.setProperty("timestamp", LocalDateTime.now());
		return problemDetail;
	}

}
