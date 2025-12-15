package com.home.infrastructure.web.detail.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.home.domain.trade.Trade;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TradeResponse(
	Long parcelId,
	List<TradeListResponse> trades
) {
	public static TradeResponse from(Long parcelId, List<Trade> trades) {
		if (trades == null) {
			trades = new ArrayList<>();
		}

		List<TradeListResponse> tradeList = trades.stream()
			.map(trade -> new TradeListResponse(
				trade.getDealDate(),
				trade.getExclArea(),
				trade.getDealAmount(),
				trade.getAptDong(),
				trade.getFloor()))
			.collect(Collectors.toList());

		return new TradeResponse(parcelId, tradeList);
	}

}
