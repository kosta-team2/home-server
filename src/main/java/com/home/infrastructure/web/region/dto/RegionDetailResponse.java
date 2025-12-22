package com.home.infrastructure.web.region.dto;

import java.util.List;

import com.home.domain.region.Region;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegionDetailResponse {
	private Long id;
	private String name;
	private Double latitude;
	private Double longitude;
	private List<ChildRegion> children;

	@AllArgsConstructor
	@Getter
	public static class ChildRegion {
		private Long id;
		private String name;
	}

	public static RegionDetailResponse of(Region region, List<Region> children) {
		List<ChildRegion> childRegions = children.stream()
			.map(child -> new ChildRegion(child.getId(), child.getRegionName()))
			.toList();

		return new RegionDetailResponse(
			region.getId(),
			region.getRegionName(),
			region.getLatitude(),
			region.getLongitude(),
			childRegions
		);
	}

}
