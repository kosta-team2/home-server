package com.home.domain.region;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegionLevelTest {

	@Test
	@DisplayName("유요한 읍면동 범위의 값을 전달했을 때 정상 생성된다.")
	void create_level_valid_success() {
		//given
		String lawdCode = "1100011000";

		//when
		RegionLevel regionLevel = RegionLevel.from(lawdCode);

		//then
		assertThat(regionLevel).isEqualTo(RegionLevel.EUP_MYEON_DONG);
	}

	@Test
	@DisplayName("맨 뒷자리가 00이 아닌 리일 경우 IllegalArgumentException을 던진다")
	void create_level_is_ri_IllegalArgumentException() {
		//given
		String lawdCode = "1100011111";

		// when && then
		assertThatThrownBy(() -> RegionLevel.from(lawdCode))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("유효하지 않은 법정동 코드일 경우 IllegalArgumentException을 던진다")
	void create_level_not_valid_IllegalArgumentException() {
		//given
		String lawdCode = "A101011010";

		// when && then
		assertThatThrownBy(() -> RegionLevel.from(lawdCode))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
