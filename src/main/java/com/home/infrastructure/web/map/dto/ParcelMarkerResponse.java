package com.home.infrastructure.web.map.dto;

public record ParcelMarkerResponse(
	Long parcelId,
	Double latitude,
	Double longitude,
	Long latestDealAmount,
	Long unitCntSum
) {
}
