package com.home.infrastructure.web.region.dto;

import com.home.domain.region.Region;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegionResponse {
	private Long id;
	private String name;
	private Double latitude;
	private Double longitude;
	private Long parentId;

	public static RegionResponse from(Region region) {
		Long parentId = (region.getParent() != null)
			? region.getParent().getId()
			: null;

		return new RegionResponse(
			region.getId(),
			region.getRegionName(),
			region.getLatitude(),
			region.getLongitude(),
			parentId
		);
	}
}
