package com.home.infrastructure.external.vworld.dto;

/**
 * VWorld 행정구역(Data API) 응답에서 추출한 대표 좌표 DTO
 *
 * @param longitude 경도 (x, EPSG:4326)
 * @param latitude  위도 (y, EPSG:4326)
 */
public record VworldCoordDto(double longitude, double latitude) {
}
