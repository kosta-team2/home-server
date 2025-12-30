package com.home.infrastructure.web.rankings.dto;

import java.util.List;

import com.home.domain.rangkings.TradeTopPrice;

public record TopPriceRankResponse(
	int rank,
	Long parcelId,
	String tradeName,
	Long maxPrice,
	Double lat,
	Double lng
) {
	public static TopPriceRankResponse from(TradeTopPrice entity) {
		return new TopPriceRankResponse(
			entity.getRank(),
			entity.getComplex().getParcel().getId(),
			entity.getComplex().getTradeName(),
			entity.getMaxPrice(),
			entity.getComplex().getParcel().getLatitude(),
			entity.getComplex().getParcel().getLongitude()
		);
	}

	public static List<TopPriceRankResponse> from(List<TradeTopPrice> entities) {
		return entities.stream()
			.map(TopPriceRankResponse::from)
			.toList();
	}
}
