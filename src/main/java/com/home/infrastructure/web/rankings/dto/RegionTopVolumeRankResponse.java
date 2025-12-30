package com.home.infrastructure.web.rankings.dto;

import java.util.List;

import com.home.domain.rangkings.TradeTopVolume;

public record RegionTopVolumeRankResponse(
	Long regionId,
	String regionName,
	int rank,
	int dealCount,
	Long parcelId,
	String tradeName
) {
	public static RegionTopVolumeRankResponse from(TradeTopVolume entity) {
		return new RegionTopVolumeRankResponse(
			entity.getRegionId(),
			entity.getRegion().getRegionName(),
			entity.getRank(),
			entity.getDealCount(),
			entity.getComplex().getParcel().getId(),
			entity.getComplex().getTradeName()
		);
	}

	public static List<RegionTopVolumeRankResponse> from(List<TradeTopVolume> entities) {
		return entities.stream()
			.map(RegionTopVolumeRankResponse::from)
			.toList();
	}
}
