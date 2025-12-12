package com.home.infrastructure.external.vworld.dto;

/**
 * VWorld 건물통합 WFS → 내부 전용 DTO
 *
 * @param longitude 경도 (x, EPSG:4326)
 * @param latitude  위도 (y, EPSG:4326)
 */
public record VworldgisDto(double longitude, double latitude) {
}
