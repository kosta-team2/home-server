package com.home.application.region;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.region.Region;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.ExternalApiException;
import com.home.infrastructure.batch.region.dto.RegionCsvRowResponse;
import com.home.infrastructure.external.vworld.VworldClient;
import com.home.infrastructure.external.vworld.dto.VworldAreaCoordinateResponse;
import com.home.infrastructure.external.vworld.dto.VworldAreaResponse;

@MockTest
class RegionCreateUseCaseTest {

	@Mock
	private RegionRepository repository;

	@Mock
	private VworldClient vworldClient;

	@InjectMocks
	private RegionCreateUseCase useCase;

	@Test
	@DisplayName("유효한 값일 때 행정 구역을 정상 생성한다.")
	void importRegions_valid_success() {
		//given
		RegionCsvRowResponse row = new RegionCsvRowResponse(
			"1100000000",
			"서울특별시"
		);

		//mock 데이터 생성
		VworldAreaResponse areaResponse = mock(VworldAreaResponse.class);
		VworldAreaCoordinateResponse coordinate =
			new VworldAreaCoordinateResponse(127.0, 37.5);

		//mock 데이터 저장
		given(vworldClient.getAdminArea(anyString(), anyString()))
			.willReturn(areaResponse);
		given(areaResponse.toCoordinate())
			.willReturn(coordinate);

		//when
		useCase.importRegions(List.of(row));

		//then
		// VWorld 호출이 1번 발생했는지 검증
		verify(vworldClient, times(1))
			.getAdminArea(anyString(), anyString());

		// RegionRepository.save가 1번 호출됐는지 검증
		verify(repository, times(1))
			.save(any(Region.class));

	}

	@Test
	@DisplayName("NOTFOUND ExternalApiException 발생하면 에러가 던지지 않고 넘어간다.")
	void importRegions_NotFound_NotThrow() {
		// given
		RegionCsvRowResponse row = new RegionCsvRowResponse(
			"1100000000",
			"서울특별시"
		);

		given(vworldClient.getAdminArea(anyString(), anyString()))
			.willThrow(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR));

		// when && then
		//예외가 발생되지 않아야 한다.
		assertThatCode(() -> useCase.importRegions(List.of(row)))
			.doesNotThrowAnyException();

		//save가 호출되지 않는다.
		verify(repository, never()).save(any(Region.class));

	}

	@Test
	@DisplayName("시군구(SIGUNGU) 레벨일 때 상위 SIDO를 찾아서 Region을 생성한다.")
	void importRegions_sigungu_success() {
		// given
		RegionCsvRowResponse row = new RegionCsvRowResponse(
			"4111100000",
			"경기도 수원시 장안구"
		);

		Region parentSido = Region.create(
			"4100000000",
			"경기도",
			RegionLevel.SIDO,
			127.0,
			37.5,
			null
		);

		given(repository.findByFullRegionName("경기도"))
			.willReturn(java.util.Optional.of(parentSido));

		VworldAreaResponse areaResponse = mock(VworldAreaResponse.class);
		VworldAreaCoordinateResponse coord =
			new VworldAreaCoordinateResponse(127.1, 37.6);

		given(vworldClient.getAdminArea(anyString(), anyString()))
			.willReturn(areaResponse);
		given(areaResponse.toCoordinate())
			.willReturn(coord);

		// when
		useCase.importRegions(List.of(row));

		// then
		verify(repository).findByFullRegionName("경기도");
		verify(vworldClient).getAdminArea("LT_C_ADSIGG_INFO", "sig_cd:=:41111");
		verify(repository).save(any(Region.class));
	}

	@Test
	@DisplayName("읍면동(EUP_MYEON_DONG) 레벨일 때 상위 시군구를 찾아서 Region을 생성한다.")
	void importRegions_eupMyeonDong_success() {
		// given
		RegionCsvRowResponse row = new RegionCsvRowResponse(
			"4111113400",
			"경기도 수원시 장안구 영화동"
		);

		Region parentSido = Region.create(
			"4100000000",
			"경기도",
			RegionLevel.SIDO,
			127.0,
			37.5,
			null
		);

		Region parentSigungu = Region.create(
			"4111100000",
			"경기도 수원시 장안구",
			RegionLevel.SIGUNGU,
			127.01,
			37.51,
			parentSido
		);

		given(repository.findByFullRegionName("경기도 수원시 장안구"))
			.willReturn(java.util.Optional.of(parentSigungu));

		VworldAreaResponse areaResponse = mock(VworldAreaResponse.class);
		VworldAreaCoordinateResponse coord =
			new VworldAreaCoordinateResponse(127.02, 37.52);

		given(vworldClient.getAdminArea(anyString(), anyString()))
			.willReturn(areaResponse);
		given(areaResponse.toCoordinate())
			.willReturn(coord);

		// when
		useCase.importRegions(List.of(row));

		// then
		verify(repository).findByFullRegionName("경기도 수원시 장안구");
		verify(vworldClient).getAdminArea("LT_C_ADEMD_INFO", "emd_cd:=:41111134");
		verify(repository).save(any(Region.class));
	}

}
