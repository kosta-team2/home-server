package com.home.domain.parcel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.infrastructure.web.map.dto.ParcelMarkerResponse;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
	Optional<Parcel> findByPnu(String pnu);

	@Query("""
		    SELECT new com.home.infrastructure.web.map.dto.ParcelMarkerResponse(
		        p.id,
		        p.latitude,
		        p.longitude,
		        (
		            SELECT t.dealAmount
		            FROM com.home.domain.trade.Trade t
		            JOIN t.complex tc
		            WHERE tc.parcel = p
		              AND t.id = (
		                  SELECT MAX(t3.id)
		                  FROM com.home.domain.trade.Trade t3
		                  JOIN t3.complex tc3
		                  WHERE tc3.parcel = p
		                    AND t3.dealDate = (
		                        SELECT MAX(t2.dealDate)
		                        FROM com.home.domain.trade.Trade t2
		                        JOIN t2.complex tc2
		                        WHERE tc2.parcel = p
		                    )
		              )
		        ),
		        COALESCE(SUM(c.unitCnt), 0L)
		    )
		    FROM Parcel p
		    LEFT JOIN com.home.domain.complex.Complex c ON c.parcel = p
		    WHERE p.latitude  BETWEEN :swLat AND :neLat
		      AND p.longitude BETWEEN :swLng AND :neLng
		    GROUP BY p.id, p.latitude, p.longitude
		""")
	List<ParcelMarkerResponse> findParcelMarkersByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);
}
