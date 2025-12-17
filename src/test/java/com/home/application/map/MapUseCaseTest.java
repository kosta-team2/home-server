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
import com.home.infrastructure.web.map.dto.ParcelMarkersRequest;
import com.home.infrastructure.web.map.dto.RegionMarkersResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
	@DisplayName("경계값 + 필터로 Parcel 마커를 조회하며, priceEok 필터는 원(won)으로 변환되어 전달된다")
	void getComplexesByBoundary_withFilters_priceEokConvertedToWon() {
		// given (priceEok: 18.6 ~ 80.0 => 1,860,000,000 ~ 8,000,000,000)
		ParcelMarkersRequest req = new ParcelMarkersRequest(
			10.0, 20.0, 30.0, 40.0,
			20, 44,               // pyeongMin, pyeongMax
			18.6, 80.0,           // priceEokMin, priceEokMax
			5, 12,                // ageMin, ageMax
			500L, 3000L           // unitMin, unitMax
		);

		List<ParcelMarkerResponse> expected = List.of(
			new ParcelMarkerResponse(1L, 37.5, 127.0, 1785000000L, 1234L)
		);

		long priceMinWon = 1_860_000_000L;
		long priceMaxWon = 8_000_000_000L;

		given(parcelRepository.findParcelMarkersByBoundary(
			eq(10.0), eq(20.0), eq(30.0), eq(40.0),
			eq(500L), eq(3000L),
			eq(priceMinWon), eq(priceMaxWon),
			eq(20), eq(44),
			eq(5), eq(12)
		)).willReturn(expected);

		// when
		List<ParcelMarkerResponse> result = mapUseCase.getComplexesByBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEqualTo(expected);

		verify(parcelRepository).findParcelMarkersByBoundary(
			10.0, 20.0, 30.0, 40.0,
			500L, 3000L,
			priceMinWon, priceMaxWon,
			20, 44,
			5, 12
		);
	}

	@Test
	@DisplayName("필터가 null이면 repository에도 null로 전달된다 (priceEok도 null이면 변환 없이 null)")
	void getComplexesByBoundary_nullFilters_passThroughNulls() {
		// given (필터 전부 null)
		ParcelMarkersRequest req = new ParcelMarkersRequest(
			10.0, 20.0, 30.0, 40.0,
			null, null,
			null, null,
			null, null,
			null, null
		);

		List<ParcelMarkerResponse> expected = List.of(
			new ParcelMarkerResponse(1L, 37.5, 127.0, null, 0L)
		);

		given(parcelRepository.findParcelMarkersByBoundary(
			eq(10.0), eq(20.0), eq(30.0), eq(40.0),
			isNull(), isNull(),        // unitMin, unitMax
			isNull(), isNull(),        // priceMinWon, priceMaxWon
			isNull(), isNull(),        // pyeongMin, pyeongMax
			isNull(), isNull()         // ageMin, ageMax
		)).willReturn(expected);

		// when
		List<ParcelMarkerResponse> result = mapUseCase.getComplexesByBoundary(req);

		// then
		assertThat(result).isEqualTo(expected);

		verify(parcelRepository).findParcelMarkersByBoundary(
			10.0, 20.0, 30.0, 40.0,
			null, null,
			null, null,
			null, null,
			null, null
		);
	}

	@Test
	@DisplayName("priceEok가 소수일 때 HALF_UP 반올림으로 원(won) 변환되어 전달된다")
	void getComplexesByBoundary_priceEokRounding_halfUp() {
		// given: 0.005억 * 100,000,000 = 500,000원 / 0.006억 => 600,000원
		ParcelMarkersRequest req = new ParcelMarkersRequest(
			1.0, 2.0, 3.0, 4.0,
			null, null,
			0.005, 0.006,
			null, null,
			null, null
		);

		long minWon = 500_000L;
		long maxWon = 600_000L;

		given(parcelRepository.findParcelMarkersByBoundary(
			eq(1.0), eq(2.0), eq(3.0), eq(4.0),
			isNull(), isNull(),
			eq(minWon), eq(maxWon),
			isNull(), isNull(),
			isNull(), isNull()
		)).willReturn(List.of());

		// when
		List<ParcelMarkerResponse> result = mapUseCase.getComplexesByBoundary(req);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

		verify(parcelRepository).findParcelMarkersByBoundary(
			1.0, 2.0, 3.0, 4.0,
			null, null,
			minWon, maxWon,
			null, null,
			null, null
		);
	}
}
