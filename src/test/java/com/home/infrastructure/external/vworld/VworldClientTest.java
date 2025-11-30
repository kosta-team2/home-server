package com.home.infrastructure.external.vworld;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class VworldClientTest {

	private MockWebServer server;
	private VworldClient vworldClient;

	@BeforeEach
	void setUp() throws IOException {
		server = new MockWebServer();
		server.start();

		String baseUrl = server.url("/").toString();

		vworldClient = new VworldClient(
			baseUrl,
			"DUMMY_KEY",
			"/gis",
			"data",
			"http://localhost"
		);
	}

	@AfterEach
	void tearDown() throws IOException {
		server.shutdown();
	}

	@Test
	@DisplayName("GIS 건물 통합 WFS 조회 성공")
	void getGis_success() throws InterruptedException {

		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(VWORLD_JSON));

		var res = vworldClient.getGis("4128510600111830000");

		var features = res.getFeatures();
		assertThat(features).hasSize(1);

		var feature = features.get(0);
		assertThat(feature.getBbox()).containsExactly(
			126.78883503,
			37.64976489,
			126.78943972,
			37.65006229
		);

		var props = feature.getProperties();
		assertThat(props.getBuldPrposCode()).isEqualTo("02000");
		assertThat(props.getBuldNm()).isEqualTo("백송마을");
		assertThat(props.getDongNm()).isEqualTo("309동");
		assertThat(props.getPnu()).isEqualTo("4128510600111830000");

		var req = server.takeRequest();
		assertThat(req.getPath())
			.startsWith("/gis")
			.contains("key=DUMMY_KEY")
			.contains("output=application/json")
			.contains("pnu=4128510600111830000")
			.contains("domain=http://localhost")
			.contains("srsName=EPSG:4326");
	}

	@Test
	@DisplayName("행정구역 경계 단건 조회 성공 - 시도(서울특별시)")
	void getAdminArea_success() throws InterruptedException {

		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(VWORLD_AREA_JSON));

		// 서울특별시
		var res = vworldClient.getAdminArea("LT_C_ADSIDO_INFO", "ctprvn_cd:=:11");

		assertThat(res).isNotNull();
		assertThat(res.getResponse()).isNotNull();
		assertThat(res.getResponse().getStatus()).isEqualTo("OK");
		assertThat(res.getResponse().getResult()).isNotNull();
		assertThat(res.getResponse().getResult().getFeatureCollection()).isNotNull();

		var fc = res.getResponse().getResult().getFeatureCollection();
		assertThat(fc.getType()).isEqualTo("FeatureCollection");
		assertThat(fc.getBbox()).containsExactly(
			126.76448395976817,
			37.428297477121646,
			127.18379492560013,
			37.701455283983684
		);

		var req = server.takeRequest();
		assertThat(req.getPath())
			.startsWith("/data")
			.contains("service=data")
			.contains("version=2.0")
			.contains("request=GetFeature")
			.contains("data=LT_C_ADSIDO_INFO")
			.contains("format=json")
			.contains("crs=EPSG:4326")
			.contains("geometry=true")
			.contains("attribute=true")
			.contains("size=1000")
			.contains("page=1")
			.contains("attrFilter=ctprvn_cd:%3D:11")
			.contains("key=DUMMY_KEY")
			.contains("domain=http://localhost");
	}

	private static final String VWORLD_JSON = """
		{
			"type": "FeatureCollection",
			"features": [
				{
					"type": "Feature",
					"properties": {
						"buld_prpos_code": "02000",
						"buld_nm": "백송마을",
						"dong_nm": "309동",
						"pnu": "4128510600111830000"
					},
					"bbox": [
						126.78883503,
						37.64976489,
						126.78943972,
						37.65006229
					]
				}
			]
		}
		""";

	private static final String VWORLD_AREA_JSON = """
		{
			"response": {
				"service": {
					"name": "data",
					"version": "2.0",
					"operation": "GetFeature",
					"time": "26(ms)"
				},
				"status": "OK",
				"record": {
					"total": "1",
					"current": "1"
				},
				"page": {
					"total": "1",
					"current": "1",
					"size": "1000"
				},
				"result": {
					"featureCollection": {
						"type": "FeatureCollection",
						"bbox": [
							126.76448395976817,
							37.428297477121646,
							127.18379492560013,
							37.701455283983684
						]
					}
				}
			}
		}
		""";
}
