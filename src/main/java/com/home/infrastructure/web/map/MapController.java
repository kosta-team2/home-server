package com.home.infrastructure.web.map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.map.MapUseCase;
import com.home.infrastructure.web.map.dto.MarkersRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {
	private final MapUseCase mapUseCase;

	@PostMapping("/complexes")
	public ResponseEntity<?> getComplexMarkers(@RequestBody MarkersRequest markersRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(mapUseCase.getComplexesByBoundary(markersRequest));
	}

	@PostMapping("/regions")
	public ResponseEntity<?> getRegionMarkers(
		@RequestBody MarkersRequest markersRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(mapUseCase.getAllRegionsByLevelAndBoundary(markersRequest));
	}
}
