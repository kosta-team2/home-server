package com.home.infrastructure.external.apis.dto;

/**
 * 국토부 실거래가 API → 내부 전용 DTO
 *
 * @param aptDong    아파트 동명(옵션)
 * @param aptNm      단지명(필수)
 * @param aptSeq     단지 일련번호(필수)
 * @param bonbun     법정동본번코드(옵션)
 * @param bubun      법정동부번코드(옵션)
 * @param buildYear  건축년도(옵션)
 * @param dealAmount 거래금액(문자열, 콤마/공백 포함 원본 그대로)
 * @param dealDay    계약일(필수)
 * @param dealMonth  계약월(필수)
 * @param dealYear   계약년도(필수)
 * @param exclArea  전용면적(m², 옵션)
 * @param floor      층(옵션)
 * @param jibun      지번(문자열, "140-1", "259" 등)
 * @param sggCd      시군구 코드
 * @param umdCd      읍면동 코드
 */
public record ApisAptTradeDto(
	String aptDong,
	String aptNm,
	String aptSeq,
	String bonbun,
	String bubun,
	Integer buildYear,
	String dealAmount,
	Integer dealDay,
	Integer dealMonth,
	Integer dealYear,
	Double exclArea,
	Integer floor,
	String jibun,
	String sggCd,
	String umdCd
) {
}
