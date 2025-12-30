package com.home.application.rankings;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.domain.rangkings.TradeTopPriceRepository;
import com.home.domain.rangkings.TradeTopVolumeRepository;
import com.home.infrastructure.web.rankings.dto.RegionTopVolumeRankResponse;
import com.home.infrastructure.web.rankings.dto.TopPriceRankResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingUseCase {
	private final TradeTopPriceRepository topPriceRepository;
	private final TradeTopVolumeRepository topVolumeRepository;

	public List<TopPriceRankResponse> getTopPrice() {
		return TopPriceRankResponse.from(
			topPriceRepository.findAll()
		);
	}

	public List<RegionTopVolumeRankResponse> getTopVolume() {
		return RegionTopVolumeRankResponse.from(
			topVolumeRepository.findAll()
		);
	}
}
