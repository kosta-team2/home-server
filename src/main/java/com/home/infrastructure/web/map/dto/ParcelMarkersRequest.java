package com.home.infrastructure.web.map.dto;

public record ParcelMarkersRequest(
	Double swLat, Double swLng,
	Double neLat, Double neLng,

	Integer pyeongMin, Integer pyeongMax,
	Double priceEokMin, Double priceEokMax,
	Integer ageMin, Integer ageMax,
	Long unitMin, Long unitMax
) {}
