package com.home.infrastructure.external.vworld.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * VWorld 행정구역(Data API) 응답
 * - 시도 / 시군구 / 읍면동 경계 조회 공통으로 사용
 * - featureCollection.bbox 로 전체 영역 BBOX 사용 가능
 * - features[*].properties 에 코드/명칭 정보 포함
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VworldAreaResponse {

	private InnerResponse response;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class InnerResponse {
		private String status;
		private Result result;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {

		@JsonProperty("featureCollection")
		private FeatureCollection featureCollection;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FeatureCollection {

		private String type;

		/**
		 * 전체 영역의 BBOX [minX, minY, maxX, maxY]
		 */
		private List<Double> bbox;
	}
}
