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
}
