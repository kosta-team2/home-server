package com.home.application.detail;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.trade.Trade;
import com.home.domain.trade.TradeRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.NotFoundException;
import com.home.infrastructure.web.detail.dto.DetailResponse;
import com.home.infrastructure.web.detail.dto.TradeResponse;

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
			.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "일치하는 parcel 정보가 없습니다. parcelId: " + parcelId));

		Complex complex = complexRepository.findTopByParcel_IdOrderByUseDateAsc(parcelId);
		if (complex == null) {
			throw new NotFoundException(ErrorCode.DATA_NOT_FOUND, "일치하는 complex 정보가 없습니다. parcelId: " + parcelId);
		}

		return DetailResponse.of(parcel, complex);
	}

	@Transactional(readOnly = true)
	public TradeResponse findAllTradeByParcelId(Long parcelId) {
		List<Long> complexIds = complexRepository.findAllIdsByParcel_Id(parcelId);
		if (complexIds.isEmpty()) {
			throw new NotFoundException(ErrorCode.DATA_NOT_FOUND, "일치하는 complex 정보가 없습니다. parcelId: " + parcelId);
		}
		log.info("complexIds: {}", complexIds);

		List<Trade> trades = tradeRepository.findByComplex_IdIn(complexIds);

		return TradeResponse.of(parcelId, trades);
	}

}
