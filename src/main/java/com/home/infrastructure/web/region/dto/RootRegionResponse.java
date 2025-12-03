package com.home.infrastructure.web.region.dto;

import com.home.domain.region.Region;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RootRegionResponse {
	private Long id;
	private String name;

	public static RootRegionResponse from(Region region) {
		return new RootRegionResponse(
			region.getId(),
			region.getRegionName()
		);
	}

}
