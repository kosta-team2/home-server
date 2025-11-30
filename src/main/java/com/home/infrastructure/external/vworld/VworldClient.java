package com.home.infrastructure.external.vworld;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.home.infrastructure.external.vworld.dto.VworldAreaResponse;
import com.home.infrastructure.external.vworld.dto.VworldgisResponse;

public class VworldClient {

	private final RestClient client;

	private final String vwServiceKey;
	private final String vwWfsPath;
	private final String vmDataPath;
	private final String domain;

	public VworldClient(
		@Value("${vworld.data.base-url}") String baseUrl,
		@Value("${vworld.data.vw-service-key}") String vwServiceKey,
		@Value("${vworld.data.vm-wfs-path}") String vwWfsPath,
		@Value("${vworld.data.vm-data-path}") String vmDataPath,
		@Value("${vworld.data.vm-domain}") String domain
	) {
		this.client = RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();

		this.vwServiceKey = vwServiceKey;
		this.vwWfsPath = vwWfsPath;
		this.vmDataPath = vmDataPath;
		this.domain = domain;
	}

	/**
	 * GIS 건물 통합 WFS 조회
	 * @param pnu 필자고유번호
	 */
	public VworldgisResponse getGis(String pnu) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(vwWfsPath)
				.queryParam("key", vwServiceKey)
				.queryParam("output", "application/json")
				.queryParam("pnu", pnu)
				.queryParam("domain", domain)
				.queryParam("srsName", "EPSG:4326")
				.build())
			.retrieve()
			.body(VworldgisResponse.class);
	}

	/**
	 * 행정구역 경계 단건 조회 (시/도, 시/군/구, 읍/면/동)
	 *
	 * @param dataSet    VWorld 데이터셋 명
	 *                   - 시도   : LT_C_ADSIDO_INFO
	 *                   - 시군구 : LT_C_ADSIGG_INFO
	 *                   - 읍면동 : LT_C_ADEMD_INFO
	 * @param attrFilter Data API attrFilter 표현식
	 *                   - 예: "ctprvn_cd:=:11"  (서울특별시)
	 *                   - 예: "sig_cd:=:11140" (서울 중구)
	 *                   - 예: "emdCd:=:11140123" (특정 읍/면/동)
	 */
	public VworldAreaResponse getAdminArea(String dataSet, String attrFilter) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(vmDataPath)
				.queryParam("service", "data")
				.queryParam("version", "2.0")
				.queryParam("request", "GetFeature")
				.queryParam("data", dataSet)
				.queryParam("format", "json")
				.queryParam("crs", "EPSG:4326")
				.queryParam("geometry", true)
				.queryParam("attribute", true)
				.queryParam("size", 1000)
				.queryParam("page", 1)
				.queryParam("attrFilter", attrFilter)
				.queryParam("key", vwServiceKey)
				.queryParam("domain", domain)
				.build())
			.retrieve()
			.body(VworldAreaResponse.class);
	}

}
