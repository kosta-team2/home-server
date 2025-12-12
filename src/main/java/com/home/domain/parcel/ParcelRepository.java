package com.home.domain.parcel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.infrastructure.web.map.dto.ParcelMarkerAggDto;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
	Optional<Parcel> findByPnu(String pnu);

	@Query("""
    SELECT new com.home.infrastructure.web.map.dto.ParcelMarkerAggDto(
        p.id,
        p.address,
        p.latitude,
        p.longitude,
        COUNT(c.id),
        COALESCE(SUM(c.unitCnt), 0L),
        COALESCE(AVG(c.archArea), 0.0),
        MAX(c.name),
        MAX(c.tradeName),
        MAX(c.useDate)
    )
    FROM Parcel p
    LEFT JOIN com.home.domain.complex.Complex c ON c.parcel = p
    WHERE p.latitude  BETWEEN :swLat AND :neLat
      AND p.longitude BETWEEN :swLng AND :neLng
    GROUP BY p.id, p.address, p.latitude, p.longitude
""")
	List<ParcelMarkerAggDto> findParcelMarkersByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

}
