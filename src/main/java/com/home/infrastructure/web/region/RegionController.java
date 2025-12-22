package com.home.infrastructure.web.region;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.region.RegionUseCase;
import com.home.infrastructure.web.region.dto.RegionDetailResponse;
import com.home.infrastructure.web.region.dto.RootRegionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/region")
public class RegionController {
	private final RegionUseCase regionUseCase;

	@GetMapping("/{regionId}")
	public ResponseEntity<RegionDetailResponse> getRegionsById(@PathVariable Long regionId) {
		RegionDetailResponse response = regionUseCase.getRegionInfoWithChildren(regionId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping
	public ResponseEntity<List<RootRegionResponse>> getRegionsById() {
		List<RootRegionResponse> list = regionUseCase.getRootRegion();

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

}
