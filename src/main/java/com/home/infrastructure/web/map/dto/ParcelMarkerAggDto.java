package com.home.infrastructure.web.map.dto;

import java.time.LocalDate;

public record ParcelMarkerAggDto(
	Long parcelId,
	String address,
	Double latitude,
	Double longitude,
	Long complexCnt,
	Long unitCntSum,
	Double archAreaAvg,
	String oneName,
	String oneTradeName,
	LocalDate oneUseDate
) {}
