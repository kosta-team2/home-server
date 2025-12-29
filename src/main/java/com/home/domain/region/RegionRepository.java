package com.home.domain.region;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.infrastructure.web.map.dto.RegionMarkersResponse;

public interface RegionRepository extends JpaRepository<Region, Long> {
	Optional<Region> findByFullRegionName(String fullRegionName);

	List<Region> findAllByParent_Id(Long parentId);

	List<Region> findAllByParentIsNull();

	/**
	나중에 region table에 집계 데이터가 들어간다면 이거 확장해서 쓰면 됨
	 */
	@Deprecated
	@Query("SELECT r FROM Region r " +
		"WHERE r.level = :level " +
		"AND r.latitude BETWEEN :swLat AND :neLat " +
		"AND r.longitude BETWEEN :swLng AND :neLng")
	List<Region> findAllRegionByLevelAndBoundary(
		@Param("level") RegionLevel level,
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

	@Query(value = """
    SELECT
        r.id            AS id,
        r.name   AS regionName,
        r.latitude      AS latitude,
        r.longitude     AS longitude,
        r.trend_30d      AS trend
    FROM region r
    WHERE r.level = :level
      AND r.geom IS NOT NULL
      AND r.geom && ST_MakeEnvelope(:swLng, :swLat, :neLng, :neLat, 4326)
""", nativeQuery = true)
	List<RegionMarkersResponse> findAllRegionMarkersByLevelAndBoundary(
		@Param("level") String level,
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);


	List<Region> findAllByLevel(RegionLevel level);

	Optional<Region> findBySggCodeAndEmdCode(String sggCode, String emdCode);
}
