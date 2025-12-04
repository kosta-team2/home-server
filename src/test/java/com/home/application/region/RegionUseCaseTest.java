package com.home.application.region;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.region.Region;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.ExternalApiException;
import com.home.infrastructure.web.region.dto.RegionDetailResponse;
import com.home.infrastructure.web.region.dto.RootRegionResponse;

@MockTest
class RegionUseCaseTest {

	@Mock
	private RegionRepository regionRepository;

	@InjectMocks
	private RegionUseCase regionUseCase;

	@Nested
	@DisplayName("id를 통해 지역의 좌표와 다음 레벨의 하위 지역을 불러온다")
	class GetRegionInfoWithChildren {

		@Test
		@DisplayName("성공: 존재하는 Region id면, 해당 Region + 하위 지역 목록을 응답한다")
		void success_whenRegionExists() {
			// given
			Long regionId = 1L;

			// 부모 Region (서울특별시)
			Region parent = mock(Region.class);
			given(parent.getId()).willReturn(regionId);
			given(parent.getRegionName()).willReturn("서울특별시");
			given(parent.getLatitude()).willReturn(37.56487638055266);
			given(parent.getLongitude()).willReturn(126.97413944268415);

			// 자식 Region들
			Region jongno = mock(Region.class);
			given(jongno.getId()).willReturn(2L);
			given(jongno.getRegionName()).willReturn("종로구");

			Region jung = mock(Region.class);
			given(jung.getId()).willReturn(90L);
			given(jung.getRegionName()).willReturn("중구");

			Region yongsan = mock(Region.class);
			given(yongsan.getId()).willReturn(165L);
			given(yongsan.getRegionName()).willReturn("용산구");

			// repository stub
			given(regionRepository.findById(regionId))
				.willReturn(Optional.of(parent));
			given(regionRepository.findAllByParent_Id(regionId))
				.willReturn(List.of(jongno, jung, yongsan));

			// when
			RegionDetailResponse response = regionUseCase.getRegionInfoWithChildren(regionId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(regionId);
			assertThat(response.getName()).isEqualTo("서울특별시");
			assertThat(response.getLatitude()).isEqualTo(37.56487638055266);
			assertThat(response.getLongitude()).isEqualTo(126.97413944268415);

			// children 개수 & 값 검증 (JSON 예시와 맞춰서)
			assertThat(response.getChildren())
				.hasSize(3)
				.extracting("id")
				.containsExactlyInAnyOrder(2L, 90L, 165L);

			assertThat(response.getChildren())
				.extracting("name")
				.containsExactlyInAnyOrder("종로구", "중구", "용산구");

			then(regionRepository).should().findById(regionId);
			then(regionRepository).should().findAllByParent_Id(regionId);
			then(regionRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 Region id면 EXTERNAL_DATA_NOT_FOUND 던지고, 에러코드/메시지를 함께 검증한다")
		void fail_whenRegionNotFound() {
			// given
			Long regionId = 999L;

			given(regionRepository.findById(regionId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> regionUseCase.getRegionInfoWithChildren(regionId))
				.isInstanceOf(ExternalApiException.class)
				.satisfies(ex -> {
					ExternalApiException e = (ExternalApiException) ex;
					// 🔽 에러코드 검증
					assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_DATA_NOT_FOUND);
					// 🔽 에러 메시지 원문까지 정확히 검증
					assertThat(e.getMessage())
						.isEqualTo(regionId + "의 행정구역은 존재하지 않습니다.");
				});

			then(regionRepository).should().findById(regionId);
			then(regionRepository).should(never()).findAllByParent_Id(anyLong());
			then(regionRepository).shouldHaveNoMoreInteractions();
		}
	}

	@Nested
	@DisplayName("시도 지역의 id, 이름, 좌표값을 불러온다")
	class GetRootRegion {

		@Test
		@DisplayName("성공: parent가 null인 모든 Region을 RootRegionResponse 리스트로 변환하여 반환한다")
		void success_whenRootRegionsExist() {
			// given
			Region root1 = mock(Region.class);
			Region root2 = mock(Region.class);

			given(root1.getId()).willReturn(1L);
			given(root1.getRegionName()).willReturn("서울특별시");
			given(root2.getId()).willReturn(456L);
			given(root2.getRegionName()).willReturn("경기도");

			given(regionRepository.findAllByParentIsNull())
				.willReturn(List.of(root1, root2));

			// when
			List<RootRegionResponse> result = regionUseCase.getRootRegion();

			// then
			assertThat(result)
				.isNotNull()
				.hasSize(2);

			assertThat(result)
			    .extracting("id")
			    .containsExactlyInAnyOrder(1L, 456L);
			assertThat(result)
			    .extracting("name")
			    .containsExactlyInAnyOrder("서울특별시", "경기도");

			then(regionRepository).should().findAllByParentIsNull();
			then(regionRepository).shouldHaveNoMoreInteractions();
		}

	}
}
