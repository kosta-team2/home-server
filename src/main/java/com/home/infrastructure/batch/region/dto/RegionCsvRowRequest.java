package com.home.infrastructure.batch.region.dto;

/**
 * 법정동 코드 CSV 한 줄을 표현하는 DTO
 * 예: 1114015000,서울특별시 중구 을지로4가,존재
 */
public record RegionCsvRowRequest(
	String lawdCode,
	String fullName,
	String status
) {
}
