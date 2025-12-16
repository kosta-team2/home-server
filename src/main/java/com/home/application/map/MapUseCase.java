package com.home.application.map;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.domain.parcel.ParcelRepository;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.MapApiException;
import com.home.infrastructure.web.map.dto.ComplexMarkersResponse;
import com.home.infrastructure.web.map.dto.MarkersRequest;
import com.home.infrastructure.web.map.dto.RegionMarkersResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapUseCase {
	private final RegionRepository regionRepository;
	private final ParcelRepository parcelRepository;

	public List<RegionMarkersResponse> getAllRegionsByLevelAndBoundary(MarkersRequest req) {
		RegionLevel regionLevel;
		if ("si-do".equals(req.region())) {
			regionLevel = RegionLevel.SIDO;
		} else if ("si-gun-gu".equals(req.region())) {
			regionLevel = RegionLevel.SIGUNGU;
		} else if ("eup-myeon-dong".equals(req.region())) {
			regionLevel = RegionLevel.EUP_MYEON_DONG;
		} else {
			throw new MapApiException(ErrorCode.INVALID_PARAMETER);
		}
		return RegionMarkersResponse.from(
			regionRepository.findAllRegionByLevelAndBoundary(regionLevel, req.swLat(), req.swLng(),
				req.neLat(), req.neLng()));
	}

	public List<ComplexMarkersResponse> getComplexesByBoundary(MarkersRequest req) {
		return ComplexMarkersResponse.fromParcelAgg(
			parcelRepository.findParcelMarkersByBoundary(req.swLat(), req.swLng(), req.neLat(), req.neLng())
		);
	}
}
