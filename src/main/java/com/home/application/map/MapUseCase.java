package com.home.application.map;

import java.util.List;

import org.springframework.stereotype.Service;

import com.home.domain.parcel.ParcelRepository;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.external.MapApiException;
import com.home.infrastructure.web.map.dto.MarkersRequest;

import com.home.infrastructure.web.map.dto.ParcelMarkerResponse;
import com.home.infrastructure.web.map.dto.ParcelMarkersRequest;
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
		RegionLevel level = switch (req.region()) {
			case "si-do" -> RegionLevel.SIDO;
			case "si-gun-gu" -> RegionLevel.SIGUNGU;
			case "eup-myeon-dong" -> RegionLevel.EUP_MYEON_DONG;
			default -> throw new MapApiException(ErrorCode.INVALID_PARAMETER);
		};

		return regionRepository.findMarkersByLevelAndBoundary(
			level.name(),
			req.swLat(), req.swLng(), req.neLat(), req.neLng()
		);
	}

	public List<ParcelMarkerResponse> getComplexesByBoundary(ParcelMarkersRequest req) {
		return parcelRepository.findParcelMarkersByBoundary(
			req.swLat(), req.swLng(), req.neLat(), req.neLng(),
			req.unitMin(), req.unitMax(),
			eokToWon(req.priceEokMin()), eokToWon(req.priceEokMax()),
			req.pyeongMin(), req.pyeongMax(),
			req.ageMin(), req.ageMax()
		);
	}

	private Long eokToWon(Double eok) {
		if (eok == null) return null;
		return java.math.BigDecimal.valueOf(eok)
			.multiply(java.math.BigDecimal.valueOf(100_000_000L))
			.setScale(0, java.math.RoundingMode.HALF_UP)
			.longValue();
	}
}
