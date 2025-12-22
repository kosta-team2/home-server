package com.home.infrastructure.external.apis.dto;

/**
 * 국토부 건축HUB 총괄표제부 → 내부 전용 DTO
 *
 * @param platPlc       대지위치
 * @param sigunguCd     시군구코드
 * @param bjdongCd      법정동코드
 * @param bldNm         건물명
 * @param platArea      대지면적(㎡)
 * @param archArea      건축면적(㎡)
 * @param bcRat         건폐율(%)
 * @param totArea       연면적(㎡)
 * @param vlRat         용적률(%)
 * @param mainPurpsCd   주용도코드 : 02000(공동주택)
 * @param hhldCnt       세대수(세대)
 */
public record ApisBldRecapDto(
	String platPlc,
	String sigunguCd,
	String bjdongCd,
	String bldNm,
	Double platArea,
	Double archArea,
	Double bcRat,
	Double totArea,
	Double vlRat,
	String mainPurpsCd,
	Integer hhldCnt
) {
}
