package com.home.infrastructure.external.apis.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 국토교통부_건축HUB_건축물대장정보 서비스:총괄표제부 조회 DTO
 * 필수 (1), 옵션(0)
 * */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApisBldRecapResponse {

	@JsonIgnoreProperties(ignoreUnknown = true)
	private Response response;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response {
		private Header header;
		private Body body;
	}

	/**
	 * resultCode: 결과코드 (00 = 정상)
	 * resultMsg : 결과메시지 (NORMAL SERVICE = 정상)
	 */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Header {
		private String resultCode;
		private String resultMsg;
	}

	/**
	 * numOfRows : 한 페이지 결과 수
	 * pageNo    : 페이지 번호
	 * totalCount: 전체 결과 수
	 */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body {
		private Items items;
		private String numOfRows;
		private String pageNo;
		private String totalCount;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {
		private List<Item> item;
	}

	/**
	 * platPlc: 대지위치(1)
	 * sigunguCd: 시군구코드(1)
	 * bjdongCd: 법정동코드(1)
	 * mgmBldrgstPk: 관리건축물대장PK(1)
	 * bldNm: 건물명(0)
	 * platArea: 대지면적(㎡)(0)
	 * archArea: 건축면적(㎡)(0)
	 * bcRat: 건폐율(%)(0)
	 * totArea: 연면적(㎡)(0)
	 * vlRat: 용적률(%)(0)
	 * mainPurpsCd: 주용도코드(0)
	 * mainPurpsCdNm: 주용도코드명(0)
	 * hhldCnt: 세대수(세대)(0)
	 */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		private String platPlc;
		private String sigunguCd;
		private String bjdongCd;
		private String mgmBldrgstPk;
		private String bldNm;
		private Double platArea;
		private Double archArea;
		private Double bcRat;
		private Double totArea;
		private Double vlRat;
		private String mainPurpsCd;
		private String mainPurpsCdNm;
		private Integer hhldCnt;
	}

	public List<ApisBldRecapDto> toApisDto() {
		if (response == null ||
			response.getHeader() == null ||
			response.getBody() == null ||
			response.getBody().getItems() == null ||
			response.getBody().getItems().getItem() == null) {
			return List.of();
		}

		return response.getBody().getItems().getItem().stream()
			.filter(item -> "02000".equals(item.getMainPurpsCd()))
			.map(item -> new ApisBldRecapDto(
				item.getPlatPlc(),
				item.getSigunguCd(),
				item.getBjdongCd(),
				item.getBldNm(),
				item.getPlatArea(),
				item.getArchArea(),
				item.getBcRat(),
				item.getTotArea(),
				item.getVlRat(),
				item.getMainPurpsCd(),
				item.getHhldCnt()
			))
			.toList();
	}

}
