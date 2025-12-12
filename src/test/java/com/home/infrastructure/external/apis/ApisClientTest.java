package com.home.infrastructure.external.apis;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.home.infrastructure.external.apis.dto.ApisAptTradeResponse;
import com.home.infrastructure.external.apis.dto.ApisBldRecapResponse;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class ApisClientTest {

	private MockWebServer server;
	private ApisClient apisClient;

	@BeforeEach
	void setUp() throws IOException {
		server = new MockWebServer();
		server.start();

		String baseUrl = server.url("/").toString();

		apisClient = new ApisClient(
			baseUrl,
			"DUMMY_APT_KEY",
			"/RTMSDataSvcAptTradeDev",
			"DUMMY_BLD_KEY",
			"/BldRgstHubService/getBrRecapTitleInfo",
			"/BldRgstHubService/getBrTitleInfo"
		);
	}

	@AfterEach
	void shutdown() throws IOException {
		server.shutdown();
	}

	@Test
	@DisplayName("국토교통부_아파트 매매 실거래가 상세 자료 단지 정보 조회 성공")
	void getAptTrade_success() throws InterruptedException {
		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(APT_TRADE_JSON));

		ApisAptTradeResponse res = apisClient.getAptTrade(
			1,
			1000,
			"41117",
			"202511"
		);

		var response = res.getResponse();
		assertThat(response.getHeader().getResultCode()).isEqualTo("000");
		assertThat(response.getHeader().getResultMsg()).isEqualTo("OK");

		var body = response.getBody();
		assertThat(body.getNumOfRows()).isEqualTo(1000);
		assertThat(body.getPageNo()).isEqualTo(1);
		assertThat(body.getTotalCount()).isEqualTo(1);

		var item = body.getItems().getItem().get(0);
		assertThat(item.getAptDong()).isBlank();
		assertThat(item.getAptNm()).isEqualTo("광교호반베르디움");
		assertThat(item.getAptSeq()).isEqualTo("41117-298");
		assertThat(item.getBonbun()).isEqualTo("0606");
		assertThat(item.getBubun()).isEqualTo("0000");
		assertThat(item.getBuildYear()).isEqualTo(2014);
		assertThat(item.getDealYear()).isEqualTo(2025);
		assertThat(item.getDealMonth()).isEqualTo(11);
		assertThat(item.getDealDay()).isEqualTo(20);

		var request = server.takeRequest();
		assertThat(request.getPath())
			.startsWith("/RTMSDataSvcAptTradeDev")
			.contains("_type=json")
			.contains("LAWD_CD=41117")
			.contains("DEAL_YMD=202511")
			.contains("pageNo=1")
			.contains("numOfRows=1000");
	}

	@Test
	@DisplayName("국토교통부_건축HUB_건축물대장정보 서비스:총괄표제부 조회 성공")
	void getBldRecap_success() throws InterruptedException {
		server.enqueue(new MockResponse()
			.setResponseCode(200)
			.setHeader("Content-Type", "application/json")
			.setBody(BLD_RECAP_JSON));

		ApisBldRecapResponse res = apisClient.getBldRecap(
			"41285",
			"10600",
			"1183",
			"0000"
		);

		var response = res.getResponse();
		assertThat(response.getHeader().getResultCode()).isEqualTo("00");
		assertThat(response.getHeader().getResultMsg()).isEqualTo("NORMAL SERVICE");

		var body = response.getBody();
		assertThat(body.getNumOfRows()).isEqualTo("100");
		assertThat(body.getPageNo()).isEqualTo("1");
		assertThat(body.getTotalCount()).isEqualTo("1");

		var item = body.getItems().getItem().get(0);
		assertThat(item.getPlatPlc()).isEqualTo("경기도 고양시 일산동구 백석동 1183번지");
		assertThat(item.getSigunguCd()).isEqualTo("41285");
		assertThat(item.getBjdongCd()).isEqualTo("10600");
		assertThat(item.getMgmBldrgstPk()).isEqualTo("11041119102");
		assertThat(item.getHhldCnt()).isEqualTo(862);

		// 이번 케이스처럼 0으로 내려오는 값은 Double 0.0으로 매핑되는지 확인
		assertThat(item.getBcRat()).isEqualTo(0.0);
		assertThat(item.getVlRat()).isEqualTo(0.0);

		var request = server.takeRequest();
		assertThat(request.getPath())
			.startsWith("/BldRgstHubService/getBrRecapTitleInfo")
			.contains("sigunguCd=41285")
			.contains("bjdongCd=10600")
			.contains("bun=1183")
			.contains("ji=0000");

	}

	private static final String APT_TRADE_JSON = """
		{
			"response": {
				"header": {
					"resultCode": "000",
					"resultMsg": "OK"
				},
				"body": {
					"items": {
						"item": [
							{
								"aptDong": " ",
								"aptNm": "광교호반베르디움",
								"aptSeq": "41117-298",
								"bonbun": "0606",
								"bubun": "0000",
								"buildYear": 2014,
								"buyerGbn": "개인",
								"cdealDay": " ",
								"cdealType": " ",
								"dealAmount": "128,500",
								"dealDay": 20,
								"dealMonth": 11,
								"dealYear": 2025,
								"dealingGbn": "중개거래",
								"estateAgentSggNm": "경기 수원시 영통구",
								"excluUseAr": 84.9424,
								"floor": 10,
								"jibun": 606,
								"landCd": 1,
								"landLeaseholdGbn": "N",
								"rgstDate": " ",
								"roadNm": "광교호수공원로",
								"roadNmBonbun": "00045",
								"roadNmBubun": "00000",
								"roadNmCd": 3350755,
								"roadNmSeq": "00",
								"roadNmSggCd": 41117,
								"roadNmbCd": " ",
								"sggCd": 41117,
								"slerGbn": "개인",
								"umdCd": 10200,
								"umdNm": "원천동"
							}
						]
					},
					"numOfRows": 1000,
					"pageNo": 1,
					"totalCount": 1
				}
			}
		}
		""";

	private static final String BLD_RECAP_JSON = """
		{
		"response": {
			"header": {
				"resultCode": "00",
				"resultMsg": "NORMAL SERVICE"
			},
			"body": {
				"items": {
					"item": [
					{
						"rnum": 1,
						"platPlc": "경기도 고양시 일산동구 백석동 1183번지",
					"sigunguCd": "41285",
					"bjdongCd": "10600",
					"platGbCd": "0",
					"bun": "1183",
					"ji": "0000",
					"mgmBldrgstPk": 11041119102,
					"regstrGbCd": "2",
					"regstrGbCdNm": "집합",
					"regstrKindCd": "1",
					"regstrKindCdNm": "총괄표제부",
					"newOldRegstrGbCd": "1",
					"newOldRegstrGbCdNm": "신대장",
					"newPlatPlc": "경기도 고양시 일산동구 백석로 109 (백석동)",
					"bldNm": " ",
					"splotNm": " ",
					"block": " ",
					"lot": " ",
					"bylotCnt": 0,
					"naRoadCd": "412853193044",
					"naBjdongCd": "10601",
					"naUgrndCd": "0",
					"naMainBun": "109",
					"naSubBun": "0",
					"platArea": 0,
					"archArea": 6045.698,
					"bcRat": 0,
					"totArea": 54895.177,
					"vlRatEstmTotArea": 51155.39,
					"vlRat": 0,
					"mainPurpsCd": "02000",
					"mainPurpsCdNm": "공동주택",
					"etcPurps": "공동주택",
					"hhldCnt": 862,
					"fmlyCnt": 0,
					"mainBldCnt": 14,
					"atchBldCnt": 0,
					"atchBldArea": 0,
					"totPkngCnt": 0,
					"indrMechUtcnt": 0,
					"indrMechArea": 0,
					"oudrMechUtcnt": 0,
					"oudrMechArea": 0,
					"indrAutoUtcnt": 0,
					"indrAutoArea": 0,
					"oudrAutoUtcnt": 0,
					"oudrAutoArea": 0,
					"pmsDay": " ",
					"stcnsDay": " ",
					"useAprDay": " ",
					"pmsnoYear": " ",
					"pmsnoKikCd": " ",
					"pmsnoKikCdNm": " ",
					"pmsnoGbCd": " ",
					"pmsnoGbCdNm": " ",
					"hoCnt": 0,
					"engrGrade": " ",
					"engrRat": 0,
					"engrEpi": 0,
					"gnBldGrade": " ",
					"gnBldCert": 0,
					"itgBldGrade": " ",
					"itgBldCert": 0,
					"crtnDay": "20220909"
					}
				]
				},
				"numOfRows": "100",
				"pageNo": "1",
				"totalCount": "1"
			}
			}
		}
		""";

}
