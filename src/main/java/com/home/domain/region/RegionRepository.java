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

	@Query("""
		    SELECT new com.home.infrastructure.web.map.dto.RegionMarkersResponse(
		        r.id, r.regionName, r.latitude, r.longitude,
		        COALESCE(CAST(SUM(c.unitCnt) AS long), 0L)
		    )
		    FROM Region r
		    LEFT JOIN Parcel p ON p.region = r
		    LEFT JOIN Complex c ON c.parcel = p
		    WHERE r.level = com.home.domain.region.RegionLevel.EUP_MYEON_DONG
		      AND r.latitude  BETWEEN :swLat AND :neLat
		      AND r.longitude BETWEEN :swLng AND :neLng
		    GROUP BY r.id, r.regionName, r.latitude, r.longitude
		""")
	List<RegionMarkersResponse> findEmdMarkersWithUnitSumByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

	@Query("""
		    SELECT new com.home.infrastructure.web.map.dto.RegionMarkersResponse(
		        r.id, r.regionName, r.latitude, r.longitude,
		        COALESCE(CAST(SUM(c.unitCnt) AS long), 0L)
		    )
		    FROM Region r
		    LEFT JOIN Parcel p ON p.region.parent = r
		    LEFT JOIN Complex c ON c.parcel = p
		    WHERE r.level = com.home.domain.region.RegionLevel.SIGUNGU
		      AND r.latitude  BETWEEN :swLat AND :neLat
		      AND r.longitude BETWEEN :swLng AND :neLng
		    GROUP BY r.id, r.regionName, r.latitude, r.longitude
		""")
	List<RegionMarkersResponse> findSigunguMarkersWithUnitSumByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

	@Query("""
		    SELECT new com.home.infrastructure.web.map.dto.RegionMarkersResponse(
		        r.id, r.regionName, r.latitude, r.longitude,
		        COALESCE(CAST(SUM(c.unitCnt) AS long), 0L)
		    )
		    FROM Region r
		    LEFT JOIN Parcel p ON p.region.parent.parent = r
		    LEFT JOIN Complex c ON c.parcel = p
		    WHERE r.level = com.home.domain.region.RegionLevel.SIDO
		      AND r.latitude  BETWEEN :swLat AND :neLat
		      AND r.longitude BETWEEN :swLng AND :neLng
		    GROUP BY r.id, r.regionName, r.latitude, r.longitude
		""")
	List<RegionMarkersResponse> findSidoMarkersWithUnitSumByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

	List<Region> findAllByLevel(RegionLevel level);

	Optional<Region> findBySggCodeAndEmdCode(String sggCode, String emdCode);
}
