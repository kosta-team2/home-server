package com.home.infrastructure.external.vworld;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.home.infrastructure.external.vworld.dto.VworldgisResponse;

public class VworldClient {

	private final RestClient client;

	private final String vwServiceKey;
	private final String vwPath;
	private final String domain;

	public VworldClient(
		@Value("${vworld.data.base-url}") String baseUrl,
		@Value("${vworld.data.vw-service-key}") String vwServiceKey,
		@Value("${vworld.data.vw-title-path}") String vwPath,
		@Value("${vworld.data.vm-domain}") String domain
	) {
		this.client = RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();

		this.vwServiceKey = vwServiceKey;
		this.vwPath = vwPath;
		this.domain = domain;
	}

	/**
	 * GIS 건물 통합 WFS 조회
	 * @param pnu 필자고유번호
	 */
	public VworldgisResponse getGis(String pnu) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(vwPath)
				.queryParam("key", vwServiceKey)
				.queryParam("output", "application/json")
				.queryParam("pnu", pnu)
				.queryParam("domain", domain)
				.queryParam("srsName", "EPSG:4326")
				.build())
			.retrieve()
			.body(VworldgisResponse.class);
	}

}
