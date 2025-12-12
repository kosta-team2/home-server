package com.home.infrastructure.external.apis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 국토교통부_아파트 매매 실거래가 상세 자료 DTO
 * 필수 (1), 옵션(0)
 * */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApisAptTradeResponse {
	private Response response;

	@Getter
	@Setter
	public static class Response {
		private Header header;
		private Body body;
	}

	/**
	 * status 200일시 통과
	 * resultCode: 결과코드(1) (000 = 정상)
	 * resultMsg : 결과메시지(1) (OK = 정상)
	 * */
	@Getter
	@Setter
	public static class Header {
		private String resultCode;
		private String resultMsg;
	}

	/**
	 * numOfRows: 한 페이지 결과 수(1)
	 * pageNo: 페이지 번호(1)
	 * totalCount: 전체 결과 수(1)
	 * */
	@Getter
	@Setter
	public static class Body {
		private Items items;
		private int numOfRows;
		private int pageNo;
		private int totalCount;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {
		private List<Item> item;
	}

	/**
	 * aptDong: 아파트 동명(0)
	 * aptNm: 단지명(1)
	 * aptSeq: 단지 일련번호(1)
	 * bonbun: 법정동본번코드(0)
	 * bubun: 법정동부번코드(0)
	 * buildYear: 건축년도(0)
	 * dealAmount: 거래금액(1)
	 * dealDay: 계약일(1)
	 * dealMonth: 계약월(1)
	 * dealYear: 계약년도(1)
	 * excluUseAr: 전용면적(0)
	 * floor: 층(0)
	 * jibun: 지번(0): int or String
	 * sggCd: 법정동시군구코드(1)
	 * umdCd: 법정동읍면동코드(1)
	 * */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		private String aptDong;
		private String aptNm;
		private String aptSeq;
		private String bonbun;
		private String bubun;
		private Integer buildYear;
		private String dealAmount;
		private Integer dealDay;
		private Integer dealMonth;
		private Integer dealYear;
		private Double excluUseAr;
		private Integer floor;
		private String jibun;
		private String sggCd;
		private String umdCd;
	}

	public List<ApisAptTradeDto> toApisDto() {
		if (response.getBody().getItems() == null) {
			return List.of();
		}

		return response.getBody().getItems().getItem().stream()
			.map(item -> new ApisAptTradeDto(
				item.getAptDong(),
				item.getAptNm(),
				item.getAptSeq(),
				item.getBonbun(),
				item.getBubun(),
				item.getBuildYear(),
				item.getDealAmount(),
				item.getDealDay(),
				item.getDealMonth(),
				item.getDealYear(),
				item.getExcluUseAr(),
				item.getFloor(),
				item.getJibun(),
				item.getSggCd(),
				item.getUmdCd()
			))
			.toList();
	}
}
