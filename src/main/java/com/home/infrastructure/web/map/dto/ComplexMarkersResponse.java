package com.home.infrastructure.web.map.dto;

import java.util.List;

import com.home.domain.complex.Complex;

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
	public static List<ComplexMarkersResponse> from(List<Complex> complexes) {
		return complexes.stream()
			.map(c -> new ComplexMarkersResponse(
				c.getId(),
				c.getName(),
				c.getAddress(),
				c.getLatitude().toString(),
				c.getLongitude().toString(),
				c.getBuildYear(),
				c.getUnitCnt(),
				c.getArchArea(),
				c.getTradeName()
			))
			.toList();
	}
}
