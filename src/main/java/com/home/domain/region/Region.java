package com.home.domain.region;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable // Hibernate: 읽기 전용 (insert/update 방지)
public class Region {
	@Id
	private Long id;

	/** 시·군·구 코드 5자리 */
	@Column(name = "sgg_code", length = 5, nullable = false)
	private String sggCode;

	/** 읍·면·동 코드 5자리 */
	@Column(name = "emd_code", length = 5)
	private String emdCode;

	/** 행정 레벨 (시·도 / 시·군·구 / 읍·면·동) */
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false)
	private RegionLevel level;

	/** 행정구역 단일 명칭 (예: 강남구, 역삼동) */
	@Column(name = "name", nullable = false)
	private String regionName;

	/** 행정구역 전체 명칭 (예: 서울특별시 강남구 역삼동) */
	@Column(name = "full_name")
	private String fullRegionName;

	/** 행정구역 대표 경도 (x, EPSG:4326 기준) */
	@Column(name = "longitude")
	private Double longitude;

	/** 행정구역 대표 위도 (y, EPSG:4326 기준) */
	@Column(name = "latitude")
	private Double latitude;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Region parent;

	@Column(name = "trend_30d")
	private Double trend30d;
}
