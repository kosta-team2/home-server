package com.home.domain.region;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RegionRepository extends JpaRepository<Region, Long> {
	Optional<Region> findByFullRegionName(String fullRegionName);
}
