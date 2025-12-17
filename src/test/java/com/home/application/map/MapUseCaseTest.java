package com.home.application.map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.region.Region;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.MapApiException;
import com.home.infrastructure.web.map.dto.MarkersRequest;
import com.home.infrastructure.web.map.dto.ParcelMarkerResponse;
import com.home.infrastructure.web.map.dto.RegionMarkersResponse;

@MockTest
class MapUseCaseTest {

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private ParcelRepository parcelRepository;

	@InjectMocks
	private MapUseCase mapUseCase;

	@Test
	@DisplayName("MarkersRequest.region이 'si-do'이면 RegionLevel.SIDO로 조회한다")
	void getAllRegionsByLevelAndBoundary_sido() {
		// given
		MarkersRequest req = new MarkersRequest(10.0, 20.0, 30.0, 40.0, "si-do");
		Region region = mock(Region.class);

		given(regionRepository.findAllRegionByLevelAndBoundary(
			eq(RegionLevel.SIDO),
			eq(10.0), eq(20.0),
			eq(30.0), eq(40.0)
		)).willReturn(List.of(region));

		// when
		List<RegionMarkersResponse> result = mapUseCase.getAllRegionsByLevelAndBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(1);
		verify(regionRepository).findAllRegionByLevelAndBoundary(
			RegionLevel.SIDO, 10.0, 20.0, 30.0, 40.0
		);
	}

	@Test
	@DisplayName("MarkersRequest.region이 'si-gun-gu'이면 RegionLevel.SIGUNGU로 조회한다")
	void getAllRegionsByLevelAndBoundary_siGunGu() {
		// given
		MarkersRequest req = new MarkersRequest(1.0, 2.0, 3.0, 4.0, "si-gun-gu");
		Region region = mock(Region.class);

		given(regionRepository.findAllRegionByLevelAndBoundary(
			eq(RegionLevel.SIGUNGU),
			eq(1.0), eq(2.0),
			eq(3.0), eq(4.0)
		)).willReturn(List.of(region));

		// when
		List<RegionMarkersResponse> result = mapUseCase.getAllRegionsByLevelAndBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(1);
		verify(regionRepository).findAllRegionByLevelAndBoundary(
			RegionLevel.SIGUNGU, 1.0, 2.0, 3.0, 4.0
		);
	}

	@Test
	@DisplayName("MarkersRequest.region이 'eup-myeon-dong'이면 RegionLevel.EUP_MYEON_DONG으로 조회한다")
	void getAllRegionsByLevelAndBoundary_eupMyeonDong() {
		// given
		MarkersRequest req = new MarkersRequest(11.0, 22.0, 33.0, 44.0, "eup-myeon-dong");
		Region region = mock(Region.class);

		given(regionRepository.findAllRegionByLevelAndBoundary(
			eq(RegionLevel.EUP_MYEON_DONG),
			eq(11.0), eq(22.0),
			eq(33.0), eq(44.0)
		)).willReturn(List.of(region));

		// when
		List<RegionMarkersResponse> result = mapUseCase.getAllRegionsByLevelAndBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(1);
		verify(regionRepository).findAllRegionByLevelAndBoundary(
			RegionLevel.EUP_MYEON_DONG, 11.0, 22.0, 33.0, 44.0
		);
	}

	@Test
	@DisplayName("MarkersRequest.region 값이 잘못되면 MapApiException(INVALID_PARAMETER)을 던진다")
	void getAllRegionsByLevelAndBoundary_invalidRegion() {
		// given
		MarkersRequest req = new MarkersRequest(0.0, 0.0, 1.0, 1.0, "invalid-region");

		// when & then
		assertThatThrownBy(() -> mapUseCase.getAllRegionsByLevelAndBoundary(req))
			.isInstanceOf(MapApiException.class)
			.satisfies(ex -> {
				MapApiException e = (MapApiException) ex;
				assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER);
			});
	}

	@Test
	@DisplayName("경계값으로 Parcel 마커(집계 결과)를 조회한다")
	void getComplexesByBoundary_success() {
		MarkersRequest req = new MarkersRequest(10.0, 20.0, 30.0, 40.0, "complex"); // region은 여기서 의미 없음

		List<ParcelMarkerResponse> expected = List.of(
			new ParcelMarkerResponse(1L, 37.5, 127.0, 1785000000L, 1234L)
		);

		given(parcelRepository.findParcelMarkersByBoundary(
			eq(10.0), eq(20.0),
			eq(30.0), eq(40.0)
		)).willReturn(expected);

		// when
		List<ParcelMarkerResponse> result = mapUseCase.getComplexesByBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(expected);

		verify(parcelRepository).findParcelMarkersByBoundary(10.0, 20.0, 30.0, 40.0);
	}
}
