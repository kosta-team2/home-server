package com.home.domain.region;

import lombok.Getter;

@Getter
public enum RegionLevel {
	/** 서울시, 경기도 */
	SIDO("시·도"),
	SIGUNGU("시·군·구"),
	EUP_MYEON_DONG("읍·면·동");

	private final String description;

	RegionLevel(String description) {
		this.description = description;
	}

	/**
	 * 규칙:
	 * - 시도(SIDO)
	 *   - 앞 2자리 제외 나머지 8자리가 모두 '0'
	 *
	 * - 시군구(SIGUNGU)
	 *   - 시군구(3자리)는 존재, 읍면동(3자리)는 "000", 리 2자리는 "00"
	 *   - 즉, 뒤 5자리가 "00000"
	 *
	 * - 읍면동(EUP_MYEON_DONG)
	 *   - 시군구(3자리), 읍면동(3자리) 모두 존재, 리 2자리는 "00"
	 *   - 즉, 읍면동 코드(6~8번째)가 "000"이 아님
	 */
	public static RegionLevel from(String lawdCode) {
		if (lawdCode == null || lawdCode.length() != 10) {
			throw new IllegalArgumentException("유효하지 않은 법정동 코드입니다: " + lawdCode);
		}

		String isRi = lawdCode.substring(8, 10);
		if (!"00".equals(isRi)) {
			throw new IllegalArgumentException("리(법정리) 코드는 RegionLevel에서 지원하지 않습니다: " + lawdCode);
		}

		String isSido = lawdCode.substring(2);
		if (isSido.equals("00000000")) {
			return SIDO;
		}

		String isSiGunGu = lawdCode.substring(5);
		if (isSiGunGu.equals("00000")) {
			return SIGUNGU;
		}

		String isEupMyeonDong = lawdCode.substring(5, 8);
		if (!isEupMyeonDong.equals("000")) {
			return EUP_MYEON_DONG;
		}

		throw new IllegalArgumentException("지원하지 않는 법정동 코드 패턴입니다: " + lawdCode);
	}
}
