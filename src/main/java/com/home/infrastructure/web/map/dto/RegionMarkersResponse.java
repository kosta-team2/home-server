package com.home.infrastructure.web.map.dto;

import java.util.List;

import com.home.domain.region.Region;

public record RegionMarkersResponse(Long id, String name, String lat, String lng) {
	public static List<RegionMarkersResponse> from(List<Region> regions) {
		return regions.stream()
			.map(r -> new RegionMarkersResponse(r.getId(), r.getRegionName(), r.getLatitude().toString(),
				r.getLongitude().toString()))
			.toList();
	}
}
