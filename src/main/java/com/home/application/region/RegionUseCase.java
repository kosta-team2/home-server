package com.home.application.region;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.domain.region.Region;
import com.home.domain.region.RegionRepository;
import com.home.infrastructure.web.region.dto.RegionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionUseCase {
	private final RegionRepository regionRepository;

	public List<RegionResponse> getRegionByParentId(Long id) {
		List<Region> list = regionRepository.findAllByParent_Id(id);

		return list.stream()
			.map(RegionResponse::from)  // Region -> RegionResponse 변환
			.toList();
	}

	public List<RegionResponse> getRegionByParentId() {
		List<Region> list = regionRepository.findAllByParentIsNull();

		return list.stream()
			.map(RegionResponse::from)  // Region -> RegionResponse 변환
			.toList();
	}
}
