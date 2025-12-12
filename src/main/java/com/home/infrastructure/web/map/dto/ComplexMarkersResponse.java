package com.home.infrastructure.web.map.dto;

import java.util.List;

public record ComplexMarkersResponse(
	Long id,
	String name,
	String address,
	String lat,
	String lng,
	Integer buildYear,
	Integer unitCnt,
	Double archArea,
	String tradeName
) {
	public static List<ComplexMarkersResponse> fromParcelAgg(List<ParcelMarkerAggDto> rows) {
		return rows.stream()
			.map(r -> {
				boolean isSingle = r.complexCnt() != null && r.complexCnt() == 1L;

				String name = isSingle ? r.oneName() : r.address();
				String tradeName = isSingle ? r.oneTradeName() : r.address();

				Integer buildYear = null;
				if (isSingle && r.oneUseDate() != null) {
					buildYear = r.oneUseDate().getYear();
				}

				return new ComplexMarkersResponse(
					r.parcelId(),
					name,
					r.address(),
					r.latitude() != null ? r.latitude().toString() : null,
					r.longitude() != null ? r.longitude().toString() : null,
					buildYear,
					r.unitCntSum() != null ? r.unitCntSum().intValue() : 0,
					r.archAreaAvg() != null ? r.archAreaAvg() : 0.0,
					tradeName
				);
			})
			.toList();
	}
}
