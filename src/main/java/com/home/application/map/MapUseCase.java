package com.home.application.map;

import com.home.domain.region.Region;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.MapApiException;
import com.home.infrastructure.web.map.dto.MarkersRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapUseCase {
	private final RegionRepository regionRepository;

	public List<Region> getAllRegionsByLevelAndBoundary(MarkersRequest req, String region) {
		//log.info("@@map use case 도착@@");
		RegionLevel regionLevel;
		if ("si-do".equals(region)) {
			regionLevel = RegionLevel.SIDO;
		} else if ("si-gun-gu".equals(region)) {
			regionLevel = RegionLevel.SIGUNGU;
		} else if ("eup-myeon-dong".equals(region)) {
			regionLevel = RegionLevel.EUP_MYEON_DONG;
		} else {
			throw new MapApiException(ErrorCode.INVALID_PARAMETER);
		}
		return regionRepository.findAllRegionByLevelAndBoundary(regionLevel, req.swLat(), req.swLng(),
			req.neLat(), req.neLng());
	}
}
