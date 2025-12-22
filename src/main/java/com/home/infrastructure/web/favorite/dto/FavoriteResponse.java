package com.home.infrastructure.web.favorite.dto;

import com.home.domain.favorite.FavoriteParcel;

public record FavoriteResponse(
	Long id,
	Long parcelId,
	String complexName,
	String address,
	Double lat,
	Double lng,
	boolean alarmEnabled
) {
	public static FavoriteResponse from(FavoriteParcel f) {
		return new FavoriteResponse(
			f.getId(),
			f.getParcel().getId(),
			f.getComplexName(),
			f.getParcel().getAddress(),
			f.getParcel().getLatitude(),
			f.getParcel().getLongitude(),
			f.isAlarmEnabled()
		);
	}
}
