package com.home.infrastructure.web.region;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.region.RegionUseCase;
import com.home.infrastructure.web.region.dto.RegionResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/move")
public class RegionController {
	private final RegionUseCase regionUseCase;

	@GetMapping("/{id}")
	public ResponseEntity<List<RegionResponse>> getRegionsById(@PathVariable Long id) {
		List<RegionResponse> list = regionUseCase.getRegionByParentId(id);

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	@GetMapping
	public ResponseEntity<List<RegionResponse>> getRegionsById() {
		List<RegionResponse> list = regionUseCase.getRegionByParentId();

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

}
