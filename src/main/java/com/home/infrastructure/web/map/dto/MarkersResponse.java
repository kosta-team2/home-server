package com.home.infrastructure.web.map.dto;

import com.home.domain.region.Region;

import java.util.List;

public record MarkersResponse(Long id, String name, String lat, String lng) {
	public static List<MarkersResponse> from(List<Region> regions) {
		return regions.stream()
			.map(r -> new MarkersResponse(r.getId(), r.getRegionName(), r.getLatitude().toString(),
				r.getLongitude().toString()))
			.toList();
	}
}
