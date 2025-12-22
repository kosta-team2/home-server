package com.home.infrastructure.external.odcloud.dto;

/**
 * 한국부동산원 공동주택 단지 동정보 → 내부 전용 DTO
 *
 * @param dongName    동명_건축물대
 */
public record OdcloudDongDto(
	String dongName
) {
}
