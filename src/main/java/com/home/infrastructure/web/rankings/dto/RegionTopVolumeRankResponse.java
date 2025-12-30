package com.home.infrastructure.web.rankings.dto;

import java.util.List;

import com.home.domain.rangkings.TradeTopVolume;

public record RegionTopVolumeRankResponse(
	Long regionId,
	String regionName,
	int rank,
	int dealCount,
	Long parcelId,
	String tradeName,
	Double lat,
	Double lng
) {
	public static RegionTopVolumeRankResponse from(TradeTopVolume entity) {
		return new RegionTopVolumeRankResponse(
			entity.getRegionId(),
			entity.getRegion().getRegionName(),
			entity.getRank(),
			entity.getDealCount(),
			entity.getComplex().getParcel().getId(),
			entity.getComplex().getTradeName(),
			entity.getComplex().getParcel().getLatitude(),
			entity.getComplex().getParcel().getLongitude()
		);
	}

	public static List<RegionTopVolumeRankResponse> from(List<TradeTopVolume> entities) {
		return entities.stream()
			.map(RegionTopVolumeRankResponse::from)
			.toList();
	}
}
