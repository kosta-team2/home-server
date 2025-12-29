package com.home.infrastructure.web.map.dto;

public record RegionMarkersResponse(
	Long id,
	String name,
	Double lat,
	Double lng,
	Double trend
) {
}
