package com.home.infrastructure.batch.region.dto;

/**
 * 현재 존재하는 법정동 코드 데이터
 * - CSV 상에서 status == "존재" 인 행만 필터링한 결과
 */
public record RegionCsvRowResponse(
	String lawdCode,
	String fullName
) {
}
