package com.home.infrastructure.web.map;

import com.home.application.map.MapUseCase;
import com.home.infrastructure.web.map.dto.MarkersRequest;
import com.home.infrastructure.web.map.dto.MarkersResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {
	private final MapUseCase mapUseCase;

	@PostMapping("/get-aggregation")
	public ResponseEntity<?> getAggregatedData(
		@RequestBody MarkersRequest markersRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(mapUseCase.getAllRegionsByLevelAndBoundary(markersRequest));
	}
}
