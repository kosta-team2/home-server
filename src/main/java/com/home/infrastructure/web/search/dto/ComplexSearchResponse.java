package com.home.infrastructure.web.search.dto;

public record ComplexSearchResponse(
	Long complexId,
	String complexName,
	Long parcelId,
	Double latitude,
	Double longitude,
	String address
) {
}
