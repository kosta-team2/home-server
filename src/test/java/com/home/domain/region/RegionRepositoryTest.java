package com.home.domain.region;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.home.annotations.JpaTest;

@JpaTest
class RegionRepositoryTest {

	@Autowired
	private RegionRepository repository;

	@BeforeEach
	void setUp() {
		Region siDO = repository.save(Region.create(
			"4100000000",
			"경기도",
			RegionLevel.SIDO,
			127.11410738431212,
			37.588017782263776,
			null
		));
		Region siGunGU = repository.save(Region.create(
			"4111100000",
			"경기도 수원시 장안구",
			RegionLevel.SIGUNGU,
			127.00005764364607,
			37.31811881763367,
			siDO

		));
		repository.save(Region.create(
			"4111113400",
			"경기도 수원시 장안구 영화동",
			RegionLevel.EUP_MYEON_DONG,
			37.29209157931696,
			127.01350194107093,
			siGunGU
		));
	}

	@Test
	@DisplayName("findByFullRegionName 테스트")
	void findByFullRegionName_success() {
		//when
		Optional<Region> sido = repository.findByFullRegionName("경기도");

		// when && then
		assertThat(sido).isPresent();

	}
}
