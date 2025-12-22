package com.home.infrastructure.external.odcloud.dto;

/**
 * 한국부동산원 공동주택 단지 기본정보 → 내부 전용 DTO
 *
 * @param address      주소
 * @param complexGbCd  단지종류코드
 * @param tradeName    단지명_공시가격
 * @param name         단지명_건축물대
 * @param complexPk    단지고유번호
 * @param dongCnt      동수
 * @param pnu          필자고유번호
 * @param unitCnt      세대수
 */
public record OdcloudAptDto(
	String address,
	String complexGbCd,
	String tradeName,
	String name,
	String complexPk,
	Integer dongCnt,
	String pnu,
	Integer unitCnt
) {
}
