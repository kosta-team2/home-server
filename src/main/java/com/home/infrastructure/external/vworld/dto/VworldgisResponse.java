package com.home.infrastructure.external.vworld.dto;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.infrastructure.external.odcloud.dto.OdcloudDongDto;

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

	/**
	 * 단지의 동 리스트(Odcloud 동 정보)와 VWorld feature들을 매칭해서
	 * bbox 중앙점들의 평균값을 단지 대표 좌표로 계산.
	 *
	 * @param dongList  Odcloud에서 가져온 동 정보 리스트
	 * @return VworldgisDto (경도, 위도). 매칭되는 게 없으면 null
	 */
	public VworldgisDto toComplexCenter(List<OdcloudDongDto> dongList) {
		if (features == null || features.isEmpty() || dongList == null || dongList.isEmpty()) {
			return null;
		}

		Set<String> dongNames = dongList.stream()
			.map(OdcloudDongDto::dongName)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		double sumX = 0.0;
		double sumY = 0.0;
		int count = 0;

		for (Feature feature : features) {
			if (feature == null || feature.getBbox() == null || feature.getBbox().size() < 4) {
				continue;
			}

			Properties props = feature.getProperties();
			if (props == null) {
				continue;
			}

			/* 단지를 나타내는 코드 02000이 아니면 제외 */
			if (!"02000".equals(props.getBuldPrposCode())) {
				continue;
			}

			/* 단지에 속한 동 이름 혹은 단지 이름이 매칭되지 않으면 제외 */
			String buldNm = props.getBuldNm();
			String dongNm = props.getDongNm();

			boolean buldMatch = buldNm != null && dongNames.contains(buldNm.trim());
			boolean dongMatch = dongNm != null && dongNames.contains(dongNm.trim());

			if (!buldMatch && !dongMatch) {
				continue;
			}

			//TODO: 중복 코드 에러 재사용을 고려해보기 (util class로 빼기)
			List<Double> bbox = feature.getBbox();
			double minX = bbox.get(0);
			double minY = bbox.get(1);
			double maxX = bbox.get(2);
			double maxY = bbox.get(3);

			double centerX = (minX + maxX) / 2.0;
			double centerY = (minY + maxY) / 2.0;

			sumX += centerX;
			sumY += centerY;
			count++;
		}

		if (count == 0) {
			return null;
		}

		double avgX = sumX / count;
		double avgY = sumY / count;

		return new VworldgisDto(avgX, avgY);
	}
}
