package com.home.infrastructure.external.vworld.dto;

import java.util.List;

/**
 * VWorld 행정구역(Data API) 응답에서 추출한 대표 좌표 DTO
 */
public record VworldAreaCoordinateResponse(double longitude, double latitude) {
}
