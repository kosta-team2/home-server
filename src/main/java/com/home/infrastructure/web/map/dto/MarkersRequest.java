package com.home.infrastructure.web.map.dto;

public record MarkersRequest(Double swLat, Double swLng, Double neLat, Double neLng, String region) {
}
