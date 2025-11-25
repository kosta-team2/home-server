package com.home.infrastructure.external.odcloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.home.infrastructure.external.odcloud.dto.OdcloudAptResponse;
import com.home.infrastructure.external.odcloud.dto.OdcloudDongResponse;

@Component
public class OdcloudClient {

	private final RestClient client;

	private final String odServiceKey;
	private final String aptPath;
	private final String dongPath;

	public OdcloudClient(
		@Value("${odcloud.data.base-url}") String baseUrl,
		@Value("${odcloud.data.od-service-key}") String odServiceKey,
		@Value("${odcloud.data.apt-title-path}") String aptPath,
		@Value("${odcloud.data.dong-title-path}") String dongPath
	) {
		this.client = RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();

		this.odServiceKey = odServiceKey;
		this.aptPath = aptPath;
		this.dongPath = dongPath;
	}

	/**
	 * 한국부동산원_공동주택 단지 식별정보 기본 정보 조회 서비스
	 * @param cond [ADRES::LIKE] = 주소 LIKE 검색 (예: "백석동 1183")
	 */
	public OdcloudAptResponse getAptInfo(String cond) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(aptPath)
				.queryParam("cond[ADRES::LIKE]", cond)
				.queryParam("serviceKey", odServiceKey)
				.build())
			.retrieve()
			.body(OdcloudAptResponse.class);
	}

	/**
	 * 한국부동산원_공동주택 단지 식별정보 동정보 조회 서비스
	 * @param cond [COMPLEX_PK::EQ] = COMPLEX_PK로 동 목록 조회
	 */
	public OdcloudDongResponse getDongInfo(String cond) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(dongPath)
				.queryParam("cond[COMPLEX_PK::EQ]", cond)
				.queryParam("serviceKey", odServiceKey)
				.build())
			.retrieve()
			.body(OdcloudDongResponse.class);
	}
}
