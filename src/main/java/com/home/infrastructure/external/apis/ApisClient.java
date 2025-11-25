package com.home.infrastructure.external.apis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.home.infrastructure.external.apis.dto.ApisAptTradeResponse;
import com.home.infrastructure.external.apis.dto.ApisBldRecapResponse;

@Component
public class ApisClient {

	private final RestClient client;

	private final String aptServiceKey;
	private final String aptPath;
	private final String bldServiceKey;
	private final String bldPath;

	public ApisClient(
		@Value("${apis.data.base-url}") String baseUrl,
		@Value("${apis.data.apt-service-key}") String aptServiceKey,
		@Value("${apis.data.apt-title-path}") String aptPath,
		@Value("${apis.data.bld-service-key}") String bldServiceKey,
		@Value("${apis.data.bld-title-path}") String bldPath
	) {
		this.client = RestClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();

		this.aptServiceKey = aptServiceKey;
		this.aptPath = aptPath;
		this.bldServiceKey = bldServiceKey;
		this.bldPath = bldPath;
	}

	/**
	 * 국토교통부_아파트 매매 실거래가 상세 자료
	 * serviceKey 인증키
	 * @param pageNo 페이지번호
	 * @param numOfRows 한 페이지 결과 수
	 * @param lawdCd 지역코드(법정동코드 10자리 중 앞 5자리)
	 * @param dealYmd 계약월 ex) 202407
	 * */
	public ApisAptTradeResponse getAptTrade(
		int pageNo,
		int numOfRows,
		String lawdCd,
		String dealYmd
	) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(aptPath)
				.queryParam("_type", "json")
				.queryParam("serviceKey", aptServiceKey)
				.queryParam("LAWD_CD", lawdCd)
				.queryParam("DEAL_YMD", dealYmd)
				.queryParam("pageNo", pageNo)
				.queryParam("numOfRows", numOfRows)
				.build())
			.retrieve()
			.body(ApisAptTradeResponse.class);
	}

	/**
	 * 국토교통부_건축HUB_건축물대장정보 서비스: 총괄표제부
	 * serviceKey 인증키
	 * @param sigunguCd 시군구코드
	 * @param bjdongCd 법정동코드
	 * @param bun 번
	 * @param ji 지
	 */
	public ApisBldRecapResponse getBldRecap(
		String sigunguCd,
		String bjdongCd,
		String bun,
		String ji
	) {
		return client.get()
			.uri(uriBuilder -> uriBuilder
				.path(bldPath)
				.queryParam("_type", "json")
				.queryParam("serviceKey", bldServiceKey)
				.queryParam("sigunguCd", sigunguCd)
				.queryParam("bjdongCd", bjdongCd)
				.queryParam("bun", bun)
				.queryParam("ji", ji)
				.build())
			.retrieve()
			.body(ApisBldRecapResponse.class);
	}


}
