package com.home.infrastructure.external.apis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.home.infrastructure.external.apis.dto.ApisAptTradeResponse;
import com.home.infrastructure.external.apis.dto.ApisBldRecapResponse;
import com.home.infrastructure.external.apis.dto.ApisRecapResponse;

import lombok.extern.slf4j.Slf4j;

//TODO: api 예외 처리 해보기
@Component
@Slf4j
public class ApisClient {

	private final RestClient client;

	private final String aptServiceKey;
	private final String aptPath;
	private final String bldServiceKey;
	private final String bldPath;
	private final String recapPath;
	private final ObjectMapper objectMapper;

	public ApisClient(
		@Value("${apis.data.base-url}") String baseUrl,
		@Value("${apis.data.apt-service-key}") String aptServiceKey,
		@Value("${apis.data.apt-title-path}") String aptPath,
		@Value("${apis.data.bld-service-key}") String bldServiceKey,
		@Value("${apis.data.bld-title-path}") String bldPath,
		@Value("${apis.data.recap-title-path}") String recapPath,
		ObjectMapper objectMapper
	) {
		this.objectMapper = objectMapper;
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5_000);
		factory.setReadTimeout(5_000);

		this.client = RestClient.builder()
			.requestFactory(factory)
			.baseUrl(baseUrl)
			.build();

		this.aptServiceKey = aptServiceKey;
		this.aptPath = aptPath;
		this.bldServiceKey = bldServiceKey;
		this.bldPath = bldPath;
		this.recapPath = recapPath;
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
		try {
			String raw = client.get()
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
				.body(String.class);

			if (raw == null || raw.isBlank()) {
				return new ApisAptTradeResponse();
			}

			JsonNode root = objectMapper.readTree(raw);

			JsonNode bodyNode = root
				.path("response")
				.path("body");

			JsonNode itemsNode = bodyNode.path("items");

			if (itemsNode.isTextual()) {
				((ObjectNode) bodyNode).putNull("items");
			}

			return objectMapper.treeToValue(root, ApisAptTradeResponse.class);

		} catch (Exception e) {
			log.warn("[AptTrade] 응답 파싱 실패. p={}, n={}, l={}, d={}",
				pageNo, numOfRows, lawdCd, dealYmd, e);
			return new ApisAptTradeResponse();
		}
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
		try {
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
		} catch (RestClientException e) {
			log.warn("[BldRecap] 호출 중 예외 발생. s={}, b={}, bun={}, ji={}",
				sigunguCd, bjdongCd, bun, ji, e);
			return null;
		}
	}

	/**
	 * 국토교통부_건축HUB_건축물대장정보 서비스: 표제부
	 * serviceKey 인증키
	 * @param sigunguCd 시군구코드
	 * @param bjdongCd 법정동코드
	 * @param bun 번
	 * @param ji 지
	 */
	public ApisRecapResponse getRecap(
		String sigunguCd,
		String bjdongCd,
		String bun,
		String ji
	) {
		try {
			return client.get()
				.uri(uriBuilder -> uriBuilder
					.path(recapPath)
					.queryParam("_type", "json")
					.queryParam("serviceKey", bldServiceKey)
					.queryParam("sigunguCd", sigunguCd)
					.queryParam("bjdongCd", bjdongCd)
					.queryParam("bun", bun)
					.queryParam("ji", ji)
					.build())
				.retrieve()
				.body(ApisRecapResponse.class);
		} catch (RestClientException e) {
			log.warn("[Recap] 호출 중 예외 발생. s={}, b={}, bun={}, ji={}",
				sigunguCd, bjdongCd, bun, ji, e);
			return null;
		}
	}

}
