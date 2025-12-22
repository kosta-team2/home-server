package com.home.application.region;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.region.Region;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.NotFoundException;
import com.home.infrastructure.web.region.dto.RegionDetailResponse;
import com.home.infrastructure.web.region.dto.RootRegionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionUseCase {
	private final RegionRepository regionRepository;

	@Transactional(readOnly = true)
	public RegionDetailResponse getRegionInfoWithChildren(Long regionId) {
		Region region = regionRepository.findById(regionId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "일치하는 region 정보가 없습니다. regionId: " + regionId));

		List<Region> children = regionRepository.findAllByParent_Id(regionId);


		return RegionDetailResponse.of(region, children);
	}

	@Transactional(readOnly = true)
	public List<RootRegionResponse> getRootRegion() {
		List<Region> regions = regionRepository.findAllByParentIsNull();

		return regions.stream()
			.map(RootRegionResponse::from)
			.toList();
	}
}
