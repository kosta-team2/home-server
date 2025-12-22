package com.home.infrastructure.external.odcloud.dto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 한국부동산원_공동주택 단지 식별정보 동정보 조회 서비스
 * 필수 (1), 옵션(0)
 * */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdcloudDongResponse {
	private int currentCount;
	private List<Item> data;
	private int matchCount;
	private int page;
	private int perPage;
	private int totalCount;

	/**
	 * COMPLEX_PK: 단지고유번호(0)
	 * COMPLEX_NM1: 동명_공시가격(0)
	 * COMPLEX_NM2: 동명_건축물대(0)
	 * COMPLEX_NM3: 동명_도로명주소(0)
	 * GRND_FLR_CNT: 지상층수(0)
	 */
	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		@JsonProperty("COMPLEX_PK")
		private String complexPk;
		@JsonProperty("DONG_NM1")
		private String dongNm1;
		@JsonProperty("DONG_NM2")
		private String dongNm2;
		@JsonProperty("DONG_NM3")
		private String dongNm3;
		@JsonProperty("GRND_FLR_CNT")
		private Integer grndFlrCnt;
	}

	public List<OdcloudDongDto> toDongDto() {
		if (data == null) {
			return List.of();
		}

		return data.stream()
			.flatMap(item -> Stream.of(
					item.getDongNm1(),
					item.getDongNm2()
				)
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(OdcloudDongDto::new))
			.collect(Collectors.toList());
	}
}
