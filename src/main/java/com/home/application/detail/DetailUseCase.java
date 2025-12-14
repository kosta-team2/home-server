package com.home.application.detail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.trade.Trade;
import com.home.domain.trade.TradeRepository;
import com.home.infrastructure.web.detail.dto.ChartFilterRequest;
import com.home.infrastructure.web.detail.dto.TradeResponse;
import com.home.infrastructure.web.detail.dto.DetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DetailUseCase {
	private final ParcelRepository parcelRepository;
	private final ComplexRepository complexRepository;
	private final TradeRepository tradeRepository;

	@Transactional(readOnly = true)
	public DetailResponse findDetailByParcelId(Long parcelId) {
		Parcel parcel = parcelRepository.findById(parcelId)
			.orElseThrow(RuntimeException::new);	// todo 예외처리 not found

		List<Complex> complexes = complexRepository.findAllByParcel_Id(parcelId);
		if (complexes.isEmpty()) throw new RuntimeException();	// todo 예외처리 not found

		// 1개의 parcel에 여러 complex가 존재시 complex를 제외하고 보낸다.
		if (complexes.size() != 1) return DetailResponse.from(parcel);
		return DetailResponse.of(parcel, complexes.get(0));
	}

	@Transactional(readOnly = true)
	public TradeResponse findAllTradeByParcelId(Long parcelId) {
		List<Long> complexIds = complexRepository.findAllIdsByParcel_Id(parcelId);
		if (complexIds.isEmpty()) { throw new IllegalArgumentException(); }	//todo 예외처리
		log.info("complexIds: {}", complexIds);
		List<Trade> totalTrades = new ArrayList<>();

		for (Long complexId : complexIds) {
			List<Trade> trades = tradeRepository.findByComplex_Id(complexId);
			for (Trade trade : trades) {
				totalTrades.add(trade);
			}
		}

		return TradeResponse.from(parcelId, totalTrades);
	}

	@Transactional(readOnly = true)
	public TradeResponse findAllFilterdTradeByParcelId(Long parcelId, ChartFilterRequest request) {
		List<Long> complexIds = complexRepository.findAllIdsByParcel_Id(parcelId);
		if (complexIds.isEmpty()) { throw new IllegalArgumentException(); }	//todo 예외처리
		log.info("complexIds: {}", complexIds);
		List<Trade> totalTrades = new ArrayList<>();

		for (Long complexId : complexIds) {
			List<Trade> trades = tradeRepository.findByComplex_IdWithFilter(complexId, request.startDate(), request.endDate(), request.exclArea());
			for (Trade trade : trades) {
				totalTrades.add(trade);
			}
		}

		return TradeResponse.from(parcelId, totalTrades);
	}

}
