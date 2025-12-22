package com.home.domain.parcel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.infrastructure.web.map.dto.ParcelMarkerResponse;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
	Optional<Parcel> findByPnu(String pnu);

	@Query(value = """
SELECT
  p.id        AS id,
  p.latitude  AS lat,
  p.longitude AS lng,
  lt.deal_amount AS latestDealAmount,
  COALESCE(ua.unit_cnt_sum, 0) AS unitCntSum
FROM parcel p

LEFT JOIN (
  SELECT c.parcel_id, COALESCE(SUM(c.unit_cnt),0) AS unit_cnt_sum
  FROM complex c
  WHERE c.deleted_at IS NULL
  GROUP BY c.parcel_id
) ua ON ua.parcel_id = p.id

LEFT JOIN LATERAL (
  SELECT t.deal_amount, t.excl_area, t.deal_date
  FROM trade t
  JOIN complex c ON c.id = t.complex_id
  WHERE t.deleted_at IS NULL
    AND c.deleted_at IS NULL
    AND c.parcel_id = p.id
  ORDER BY t.deal_date DESC, t.id DESC
  LIMIT 1
) lt ON true

WHERE p.deleted_at IS NULL
  AND p.latitude  BETWEEN :swLat AND :neLat
  AND p.longitude BETWEEN :swLng AND :neLng

  AND (:unitMin IS NULL OR COALESCE(ua.unit_cnt_sum,0) >= :unitMin)
  AND (:unitMax IS NULL OR COALESCE(ua.unit_cnt_sum,0) <= :unitMax)

  AND (:priceMinWon IS NULL OR lt.deal_amount >= :priceMinWon)
  AND (:priceMaxWon IS NULL OR lt.deal_amount <= :priceMaxWon)

  AND (
    (:pyeongMin IS NULL AND :pyeongMax IS NULL)
    OR (
      lt.excl_area IS NOT NULL
      AND (:pyeongMin IS NULL OR ROUND(lt.excl_area/3.3)::int >= :pyeongMin)
      AND (:pyeongMax IS NULL OR ROUND(lt.excl_area/3.3)::int <= :pyeongMax)
    )
  )

  AND (
    (:ageMin IS NULL AND :ageMax IS NULL)
    OR EXISTS (
      SELECT 1
      FROM complex c2
      WHERE c2.deleted_at IS NULL
        AND c2.parcel_id = p.id
        AND c2.use_date IS NOT NULL
        AND (:ageMin IS NULL OR EXTRACT(YEAR FROM AGE(CURRENT_DATE, c2.use_date)) >= :ageMin)
        AND (:ageMax IS NULL OR EXTRACT(YEAR FROM AGE(CURRENT_DATE, c2.use_date)) <= :ageMax)
    )
  )
""", nativeQuery = true)
	List<ParcelMarkerResponse> findParcelMarkersByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng,

		@Param("unitMin") Long unitMin,
		@Param("unitMax") Long unitMax,

		@Param("priceMinWon") Long priceMinWon,
		@Param("priceMaxWon") Long priceMaxWon,

		@Param("pyeongMin") Integer pyeongMin,
		@Param("pyeongMax") Integer pyeongMax,

		@Param("ageMin") Integer ageMin,
		@Param("ageMax") Integer ageMax
	);
}
