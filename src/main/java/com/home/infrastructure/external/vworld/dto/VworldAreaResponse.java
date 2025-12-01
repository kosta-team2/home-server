package com.home.infrastructure.external.vworld.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.ExternalApiException;

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

	public VworldAreaCoordinateResponse toCoordinate() {
		if (response.status.trim().equals("NOT_FOUND")) {
			throw new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR, "VWorld 행정구역에 대한 정보를 찾기 못했습니다.");
		}

		FeatureCollection fc = response.getResult().getFeatureCollection();
		List<Double> bbox = fc.getBbox();

		double minX = bbox.get(0);
		double minY = bbox.get(1);
		double maxX = bbox.get(2);
		double maxY = bbox.get(3);

		double longitude = (minX + maxX) / 2.0;
		double latitude = (minY + maxY) / 2.0;

		return new VworldAreaCoordinateResponse(longitude, latitude);
	}

}
