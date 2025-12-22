package com.home.infrastructure.external.odcloud;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.home.infrastructure.external.odcloud.dto.OdcloudAptResponse;
import com.home.infrastructure.external.odcloud.dto.OdcloudDongResponse;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class OdcloudClientTest {

	private MockWebServer server;
	private OdcloudClient client;

	@BeforeEach
	void setUp() throws IOException {
		server = new MockWebServer();
		server.start();

		String baseUrl = server.url("/").toString();

		client = new OdcloudClient(
			baseUrl,
			"DUMMY_KEY",
			"/apt",
			"/dong"
		);
	}

	@AfterEach
	void tearDown() throws IOException {
		server.shutdown();
	}

	@Test
	@DisplayName("한국부동산원_공동주택 단지 식별정보 기본 정보 조회 서비스 조회 성공")
	void getAptInfo_success() throws Exception {
		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(APT_JSON)
		);

		OdcloudAptResponse res = client.getAptInfo("영통동 955-1", 1, 1000);

		assertThat(res.getCurrentCount()).isEqualTo(1);
		assertThat(res.getPage()).isEqualTo(1);
		assertThat(res.getPerPage()).isEqualTo(10);

		var item = res.getData().get(0);
		assertThat(item.getAddress()).isEqualTo("경기도 수원영통구 영통동 955-1");
		assertThat(item.getComplexGbCd()).isEqualTo("1");
		assertThat(item.getComplexNm1()).isEqualTo("황골마을주공1");
		assertThat(item.getComplexNm2()).isEqualTo("영통포레파크원 아파트");
		assertThat(item.getComplexNm3()).isEqualTo("황골마을주공1단지아파트");
		assertThat(item.getComplexPk()).isEqualTo("41117100006139");
		assertThat(item.getDongCnt()).isEqualTo(39);
		assertThat(item.getUnitCnt()).isEqualTo(3129);

		var req = server.takeRequest();
		assertThat(req.getPath())
			.startsWith("/apt")
			.contains("cond%5BADRES::LIKE%5D=")
			.contains("955-1")
			.contains("serviceKey=DUMMY_KEY");
	}

	@Test
	@DisplayName("한국부동산원_공동주택 단지 식별정보 동정보 조회 성공")
	void getDongInfo_success() throws Exception {
		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(DONG_JSON)
		);

		OdcloudDongResponse res = client.getDongInfo("41117100006139");

		assertThat(res.getCurrentCount()).isEqualTo(2);
		assertThat(res.getData()).hasSize(2);

		var first = res.getData().get(0);
		var second = res.getData().get(1);

		assertThat(first.getComplexPk()).isEqualTo("41117100006139");
		assertThat(second.getComplexPk()).isEqualTo("41117100006139");

		assertThat(first.getDongNm1()).isEqualTo("121");
		assertThat(first.getDongNm2()).isEqualTo("121동");
		assertThat(first.getDongNm3()).isEqualTo("121동");
		assertThat(first.getGrndFlrCnt()).isEqualTo(18);

		var req = server.takeRequest();
		assertThat(req.getPath())
			.startsWith("/dong")
			.contains("cond%5BCOMPLEX_PK::EQ%5D=41117100006139")
			.contains("serviceKey=DUMMY_KEY");
	}

	private static final String APT_JSON = """
		{
			"currentCount": 1,
			"data": [
				{
					"ADRES": "경기도 수원영통구 영통동 955-1",
					"COMPLEX_GB_CD": "1",
					"COMPLEX_NM1": "황골마을주공1",
					"COMPLEX_NM2": "영통포레파크원 아파트",
					"COMPLEX_NM3": "황골마을주공1단지아파트",
					"COMPLEX_PK": "41117100006139",
					"DONG_CNT": 39,
					"PNU": "4111710500109550001",
					"UNIT_CNT": 3129,
					"USEAPR_DT": "19971216"
				}
			],
			"matchCount": 1,
			"page": 1,
			"perPage": 10,
			"totalCount": 307407
		}
		""";

	private static final String DONG_JSON = """
		{
			"currentCount": 2,
			"data": [
				{
					"COMPLEX_PK": "41117100006139",
					"DONG_NM1": "121",
					"DONG_NM2": "121동",
					"DONG_NM3": "121동",
					"GRND_FLR_CNT": 18
				},
				{
					"COMPLEX_PK": "41117100006139",
					"DONG_NM1": "122",
					"DONG_NM2": "122동",
					"DONG_NM3": "122동",
					"GRND_FLR_CNT": 20
				}
			],
			"matchCount": 2,
			"page": 1,
			"perPage": 10,
			"totalCount": 464807
		}
		""";
}
