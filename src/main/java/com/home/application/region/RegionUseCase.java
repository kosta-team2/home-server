package com.home.application.region;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.domain.region.Region;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.ExternalApiException;
import com.home.infrastructure.web.region.dto.RegionDetailResponse;
import com.home.infrastructure.web.region.dto.RootRegionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionUseCase {
	private final RegionRepository regionRepository;

	public RegionDetailResponse getRegionInfoWithChildren(Long id) {
		Region region = regionRepository.findById(id)
			.orElseThrow(() -> new ExternalApiException(
				ErrorCode.EXTERNAL_DATA_NOT_FOUND,
				id + "의 행정구역은 존재하지 않습니다.")
			);

		List<Region> children = regionRepository.findAllByParent_Id(id);


		return RegionDetailResponse.of(region, children);
	}

	public List<RootRegionResponse> getRootRegion() {
		List<Region> regions = regionRepository.findAllByParentIsNull();

		return regions.stream()
			.map(RootRegionResponse::from)
			.toList();
	}
}
