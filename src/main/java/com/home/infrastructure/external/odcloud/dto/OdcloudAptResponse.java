package com.home.infrastructure.external.odcloud.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 한국부동산원_공동주택 단지 식별정보 기본 정보 조회 서비스
 * 필수 (1), 옵션(0)
 * */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdcloudAptResponse {
	private int currentCount;
	private List<Item> data;
	private int matchCount;
	private int page;
	private int perPage;
	private int totalCount;

	/**
	 * ADRES: 주소(0)
	 * COMPLEX_GB_CD: 단지종류(0)
	 * COMPLEX_NM1: 단지명_공시가격(0)
	 * COMPLEX_NM2: 단지명_건축물대(0)
	 * COMPLEX_NM3: 단지명_도로명주소(0)
	 * COMPLEX_PK: 단지고유번호(0)
	 * DONG_CNT: 동수(0)
	 * PNU: 필자고유번호(0)
	 * UNIT_CNT: 세대수(0)
	 * USEAPR_DT: 사용승인일(0)
	 * */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		@JsonProperty("ADRES")
		private String address;
		@JsonProperty("COMPLEX_GB_CD")
		private String complexGbCd;
		@JsonProperty("COMPLEX_NM1")
		private String complexNm1;
		@JsonProperty("COMPLEX_NM2")
		private String complexNm2;
		@JsonProperty("COMPLEX_NM3")
		private String complexNm3;
		@JsonProperty("COMPLEX_PK")
		private String complexPk;
		@JsonProperty("DONG_CNT")
		private Integer dongCnt;
		@JsonProperty("PNU")
		private String pnu;
		@JsonProperty("UNIT_CNT")
		private Integer unitCnt;
		@JsonProperty("USEAPR_DT")
		private String useaprDt;
	}

	public List<OdcloudAptDto> toAptDto() {
		if (data == null) {
			return List.of();
		}

		return data.stream()
			.map(item -> new OdcloudAptDto(
				item.getAddress(),
				item.getComplexGbCd(),
				item.getComplexNm1(),
				item.getComplexNm2(),
				item.getComplexPk(),
				item.getDongCnt(),
				item.getPnu(),
				item.getUnitCnt()
			))
			.toList();
	}
}
