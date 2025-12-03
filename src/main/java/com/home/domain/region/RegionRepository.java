package com.home.domain.region;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<Region, Long> {
	Optional<Region> findByFullRegionName(String fullRegionName);

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
}
