package com.home.infrastructure.external.vworld.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * VWorld GIS 건물통합 WFS 응답
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VworldgisResponse {

	private List<Feature> features;

	/**
	 * bbox: 최소 사각형 [minX, minY, maxX, maxY]
	 * */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Feature {
		private Properties properties;
		private List<Double> bbox;
	}

	/**
	 * buld_prpos_code: 건물용도코드 (02000=공동 주택)
	 * buld_nm: 단지명
	 * dong_nm: 동명
	 * pnu: 필지번호
	 * */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Properties {
		@JsonProperty("buld_prpos_code")
		private String buldPrposCode;
		@JsonProperty("buld_nm")
		private String buldNm;
		@JsonProperty("dong_nm")
		private String dongNm;
		private String pnu;
	}

}
