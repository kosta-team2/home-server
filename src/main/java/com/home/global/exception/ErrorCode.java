package com.home.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 공통 에러 코드 정의
 * - C: Client 요청 오류 (validation, 잘못된 파라미터 등, 주로 4xx)
 * - D: Domain / 비즈니스 규칙 위반 (주로 4xx)
 * - A: Auth / 인증·인가 관련 오류 (4xx)
 * - I: Infra / 외부 시스템·인프라 연동 오류 (5xx)
 * - S: Server 내부 예측 불가 오류 (5xx)
 * 번호
 * - 400 ~ 499: 클라이언트 계열 에러
 * - 500 ~ 599: 서버/인프라 계열 에러
 * */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "C400", "잘못된 요청 형식 입니다."),
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "C401", "잘못된 파라미터 형식 입니다."),
	EXTERNAL_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "I404", "외부 데이터에서 결과를 찾을 수 없습니다."),
	DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "C404", "일치하는 데이터가 없습니다."),

	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S500", "서버 오류가 발생했습니다."),
	EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "I500", "외부 서비스 연동 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String title;
	private final String detail;
}
