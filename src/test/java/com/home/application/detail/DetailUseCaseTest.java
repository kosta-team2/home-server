package com.home.application.detail;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.infrastructure.web.detail.dto.DetailResponse;

@MockTest
class DetailUseCaseTest {
	@Mock
	private ComplexRepository complexRepository;

	@InjectMocks
	private DetailUseCase detailUseCase;

	@Nested
	@DisplayName("단지 상세 조회(findById)")
	class FindById {

		@Test
		@DisplayName("성공: 존재하는 id면 Complex를 DetailResponse로 정확히 매핑하여 반환한다")
		void success_whenComplexExists() {
			// given
			Long complexId = 1L;

			// Complex 엔티티 mock
			Complex complex = mock(Complex.class);

			// DetailResponse.from() 에서 사용하는 모든 필드 stub
			given(complex.getId()).willReturn(complexId);
			given(complex.getAddress()).willReturn("서울특별시 어딘가 123");
			given(complex.getTradeName()).willReturn("힐스테이트");
			given(complex.getName()).willReturn("힐스테이트 아파트");
			given(complex.getLatitude()).willReturn(37.123456);
			given(complex.getLongitude()).willReturn(127.123456);
			given(complex.getDongCnt()).willReturn(5);
			given(complex.getUnitCnt()).willReturn(500);
			given(complex.getPlatArea()).willReturn(1000.0);
			given(complex.getArchArea()).willReturn(300.0);
			given(complex.getTotArea()).willReturn(20000.0);
			given(complex.getBcRat()).willReturn(30.0);
			given(complex.getVlRat()).willReturn(200.0);
			given(complex.getBuildYear()).willReturn(2010);

			given(complexRepository.findById(complexId))
				.willReturn(Optional.of(complex));

			// when
			DetailResponse response = detailUseCase.findById(complexId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(complexId);
			assertThat(response.getAddress()).isEqualTo("서울특별시 어딘가 123");
			assertThat(response.getTradeName()).isEqualTo("힐스테이트");
			assertThat(response.getName()).isEqualTo("힐스테이트 아파트");
			assertThat(response.getLatitude()).isEqualTo(37.123456);
			assertThat(response.getLongitude()).isEqualTo(127.123456);
			assertThat(response.getDongCnt()).isEqualTo(5);
			assertThat(response.getUnitCnt()).isEqualTo(500);
			assertThat(response.getPlatArea()).isEqualTo(1000.0);
			assertThat(response.getArchArea()).isEqualTo(300.0);
			assertThat(response.getTotArea()).isEqualTo(20000.0);
			assertThat(response.getBcRat()).isEqualTo(30.0);
			assertThat(response.getVlRat()).isEqualTo(200.0);
			assertThat(response.getBuildYear()).isEqualTo(2010);

			then(complexRepository).should().findById(complexId);
			then(complexRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 id면 RuntimeException을 던진다")
		void fail_whenComplexNotFound() {
			// given
			Long complexId = 999L;

			given(complexRepository.findById(complexId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> detailUseCase.findById(complexId))
				.isInstanceOf(RuntimeException.class); 	//todo 에러 검증

			then(complexRepository).should().findById(complexId);
			then(complexRepository).shouldHaveNoMoreInteractions();
		}
	}
}
