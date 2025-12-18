package com.home.application.detail;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.trade.Trade;
import com.home.domain.trade.TradeRepository;
import com.home.global.exception.common.NotFoundException;
import com.home.infrastructure.web.detail.dto.DetailResponse;
import com.home.infrastructure.web.detail.dto.TradeResponse;

@MockTest
class DetailUseCaseTest {

	@Mock
	private ComplexRepository complexRepository;

	@Mock
	private ParcelRepository parcelRepository;

	@Mock
	private TradeRepository tradeRepository;

	@InjectMocks
	private DetailUseCase detailUseCase;

	@Nested
	@DisplayName("단지 상세 조회 (findByParcelId)")
	class FindByParcelId {

		@Test
		@DisplayName("성공: Parcel과 Complex가 1개씩 존재하면 둘 다 DetailResponse로 매핑하여 반환한다")
		void success_whenParcelAndSingleComplexExists() {
			// given
			Long parcelId = 1L;

			Parcel parcel = mock(Parcel.class);
			given(parcel.getId()).willReturn(parcelId);
			given(parcel.getLatitude()).willReturn(37.123456);
			given(parcel.getLongitude()).willReturn(127.123456);
			given(parcel.getAddress()).willReturn("서울특별시 어딘가 123");

			given(parcelRepository.findById(parcelId))
				.willReturn(Optional.of(parcel));

			Complex complex = mock(Complex.class);
			given(complex.getTradeName()).willReturn("힐스테이트");
			given(complex.getName()).willReturn("힐스테이트 아파트");
			given(complex.getDongCnt()).willReturn(5);
			given(complex.getUnitCnt()).willReturn(500);
			given(complex.getPlatArea()).willReturn(1000.0);
			given(complex.getArchArea()).willReturn(300.0);
			given(complex.getTotArea()).willReturn(20000.0);
			given(complex.getBcRat()).willReturn(30.0);
			given(complex.getVlRat()).willReturn(200.0);
			LocalDate useDate = LocalDate.of(2010, 1, 1);
			given(complex.getUseDate()).willReturn(useDate);

			given(complexRepository.findAllByParcel_Id(parcelId))
				.willReturn(List.of(complex));

			// when
			DetailResponse response = detailUseCase.findDetailByParcelId(parcelId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getParcelId()).isEqualTo(parcelId);
			assertThat(response.getLatitude()).isEqualTo(37.123456);
			assertThat(response.getLongitude()).isEqualTo(127.123456);
			assertThat(response.getAddress()).isEqualTo("서울특별시 어딘가 123");

			assertThat(response.getTradeName()).isEqualTo("힐스테이트");
			assertThat(response.getName()).isEqualTo("힐스테이트 아파트");
			assertThat(response.getDongCnt()).isEqualTo(5);
			assertThat(response.getUnitCnt()).isEqualTo(500);
			assertThat(response.getPlatArea()).isEqualTo(1000.0);
			assertThat(response.getArchArea()).isEqualTo(300.0);
			assertThat(response.getTotArea()).isEqualTo(20000.0);
			assertThat(response.getBcRat()).isEqualTo(30.0);
			assertThat(response.getVlRat()).isEqualTo(200.0);
			assertThat(response.getUseDate()).isEqualTo(useDate);

			then(parcelRepository).should().findById(parcelId);
			then(complexRepository).should().findAllByParcel_Id(parcelId);
			then(parcelRepository).shouldHaveNoMoreInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
			then(tradeRepository).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("성공: Complex가 여러 개면 Complex 정보 없이 Parcel 정보만 DetailResponse로 반환한다")
		void success_whenMultipleComplexes_returnsParcelOnly() {
			// given
			Long parcelId = 1L;

			Parcel parcel = mock(Parcel.class);
			given(parcel.getId()).willReturn(parcelId);
			given(parcel.getLatitude()).willReturn(37.123456);
			given(parcel.getLongitude()).willReturn(127.123456);
			given(parcel.getAddress()).willReturn("서울특별시 어딘가 123");

			given(parcelRepository.findById(parcelId))
				.willReturn(Optional.of(parcel));

			Complex complex1 = mock(Complex.class);
			Complex complex2 = mock(Complex.class);
			given(complexRepository.findAllByParcel_Id(parcelId))
				.willReturn(List.of(complex1, complex2));

			// when
			DetailResponse response = detailUseCase.findDetailByParcelId(parcelId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.getParcelId()).isEqualTo(parcelId);
			assertThat(response.getLatitude()).isEqualTo(37.123456);
			assertThat(response.getLongitude()).isEqualTo(127.123456);
			assertThat(response.getAddress()).isEqualTo("서울특별시 어딘가 123");

			assertThat(response.getTradeName()).isNull();
			assertThat(response.getName()).isNull();
			assertThat(response.getDongCnt()).isNull();
			assertThat(response.getUnitCnt()).isNull();
			assertThat(response.getPlatArea()).isNull();
			assertThat(response.getArchArea()).isNull();
			assertThat(response.getTotArea()).isNull();
			assertThat(response.getBcRat()).isNull();
			assertThat(response.getVlRat()).isNull();
			assertThat(response.getUseDate()).isNull();

			then(parcelRepository).should().findById(parcelId);
			then(complexRepository).should().findAllByParcel_Id(parcelId);
			then(parcelRepository).shouldHaveNoMoreInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
			then(tradeRepository).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("실패: Parcel이 존재하지 않으면 NotFoundException을 던진다")
		void fail_whenParcelNotFound() {
			// given
			Long parcelId = 999L;

			given(parcelRepository.findById(parcelId))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> detailUseCase.findDetailByParcelId(parcelId))
				.isInstanceOf(NotFoundException.class);

			then(parcelRepository).should().findById(parcelId);
			then(complexRepository).shouldHaveNoInteractions();
			then(tradeRepository).shouldHaveNoInteractions();
			then(parcelRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("실패: Complex 목록이 비어 있으면 NotFoundException을 던진다")
		void fail_whenComplexListEmpty() {
			// given
			Long parcelId = 1L;

			Parcel parcel = mock(Parcel.class);
			given(parcelRepository.findById(parcelId))
				.willReturn(Optional.of(parcel));

			given(complexRepository.findAllByParcel_Id(parcelId))
				.willReturn(List.of());

			// when & then
			assertThatThrownBy(() -> detailUseCase.findDetailByParcelId(parcelId))
				.isInstanceOf(NotFoundException.class);

			then(parcelRepository).should().findById(parcelId);
			then(complexRepository).should().findAllByParcel_Id(parcelId);
			then(parcelRepository).shouldHaveNoMoreInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
			then(tradeRepository).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("거래 전체 조회 (findAllTradeByParcelId)")
	class FindAllTradeByParcelId {

		@Test
		@DisplayName("성공: parcelId로 complexIds를 조회하고, 해당 complexIds로 trades를 조회하여 TradeResponse로 반환한다")
		void success_whenComplexIdsExist_returnsTradeResponse() {
			// given
			Long parcelId = 1L;
			List<Long> complexIds = List.of(100L, 101L);

			Trade trade1 = mock(Trade.class);
			Trade trade2 = mock(Trade.class);
			List<Trade> trades = List.of(trade1, trade2);

			given(complexRepository.findAllIdsByParcel_Id(parcelId)).willReturn(complexIds);
			given(tradeRepository.findByComplex_IdIn(complexIds)).willReturn(trades);

			// when
			TradeResponse response = detailUseCase.findAllTradeByParcelId(parcelId);

			// then
			assertThat(response).isNotNull();
			// TradeResponse 내부 구조를 몰라도 최소한 null 아님 + 호출 검증은 가능
			then(complexRepository).should().findAllIdsByParcel_Id(parcelId);
			then(tradeRepository).should().findByComplex_IdIn(complexIds);

			then(parcelRepository).shouldHaveNoInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
			then(tradeRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("성공: trades가 비어 있어도 TradeResponse를 반환한다 (서비스는 예외를 던지지 않음)")
		void success_whenTradesEmpty_returnsTradeResponse() {
			// given
			Long parcelId = 1L;
			List<Long> complexIds = List.of(100L, 101L);

			given(complexRepository.findAllIdsByParcel_Id(parcelId)).willReturn(complexIds);
			given(tradeRepository.findByComplex_IdIn(complexIds)).willReturn(List.of());

			// when
			TradeResponse response = detailUseCase.findAllTradeByParcelId(parcelId);

			// then
			assertThat(response).isNotNull();

			then(complexRepository).should().findAllIdsByParcel_Id(parcelId);
			then(tradeRepository).should().findByComplex_IdIn(complexIds);

			then(parcelRepository).shouldHaveNoInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
			then(tradeRepository).shouldHaveNoMoreInteractions();
		}

		@Test
		@DisplayName("실패: complexIds가 비어 있으면 NotFoundException을 던진다")
		void fail_whenComplexIdsEmpty_throwsNotFoundException() {
			// given
			Long parcelId = 1L;

			given(complexRepository.findAllIdsByParcel_Id(parcelId)).willReturn(List.of());

			// when & then
			assertThatThrownBy(() -> detailUseCase.findAllTradeByParcelId(parcelId))
				.isInstanceOf(NotFoundException.class);

			then(complexRepository).should().findAllIdsByParcel_Id(parcelId);
			then(tradeRepository).shouldHaveNoInteractions();
			then(parcelRepository).shouldHaveNoInteractions();
			then(complexRepository).shouldHaveNoMoreInteractions();
		}
	}
}
