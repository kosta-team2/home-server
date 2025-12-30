package com.home.infrastructure.web.rankings;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.rankings.RankingUseCase;
import com.home.infrastructure.web.rankings.dto.RegionTopVolumeRankResponse;
import com.home.infrastructure.web.rankings.dto.TopPriceRankResponse;

@RestController
@RequestMapping("api/v1/rankings")
public class RankingsController {
	private final RankingUseCase useCase;

	public RankingsController(RankingUseCase useCase) {
		this.useCase = useCase;
	}

	@GetMapping("/top-price-30d")
	public ResponseEntity<List<TopPriceRankResponse>> topPrice() {
		return ResponseEntity.ok(useCase.getTopPrice());
	}

	@GetMapping("/top-volume-30d")
	public ResponseEntity<List<RegionTopVolumeRankResponse>> topVolume() {
		return ResponseEntity.ok(useCase.getTopVolume());
	}
}
