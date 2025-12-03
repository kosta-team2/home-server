package com.home.domain.region;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegionTest {

	@Test
	@DisplayName("유효한 값을 입력했을 때 행정구역은 정상 생성된다.")
	void create_region_valid_success() {
		//given
		String lawdCode = "4111100000";
		String fullName = "경기도 수원시 장안구";
		RegionLevel regionLevel = RegionLevel.SIGUNGU;
		Double longitude = 37.31811881763367;
		Double latitude = 127.00005764364607;

		Region parent = Region.create(
			"4100000000",
			"경기도",
			RegionLevel.SIDO,
			127.0000,
			37.5000,
			null
		);

		//when
		Region region = Region.create(
			lawdCode,
			fullName,
			regionLevel,
			longitude,
			latitude,
			parent
		);

		//then
		assertThat(region.getSggCode()).isEqualTo("41111");
		assertThat(region.getEmdCode()).isEqualTo(null);
		assertThat(region.getFullRegionName()).isEqualTo("경기도 수원시 장안구");
		assertThat(region.getRegionName()).isEqualTo("수원시 장안구");
	}

	@Test
	@DisplayName("시도 레벨이 아닐 때 parent가 null이면 IllegalArgumentException을 던진다.")
	void creat_region_fail_IllegalArgumentException() {
		// when && then
		assertThatThrownBy(() -> Region.create(
			"4111100000",
			"경기도 수원시 장안구",
			RegionLevel.SIGUNGU,
			1.1,
			1.1,
			null
		)).isInstanceOf(IllegalArgumentException.class);
	}
}
