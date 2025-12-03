package com.home.domain.region;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "region")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "region_seq",
	sequenceName = "region_id_seq",
	initialValue = 1,
	allocationSize = 1
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq")
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

	private Region(String sggCode, String emdCode, RegionLevel level, String regionName, String fullRegionName,
		Double longitude, Double latitude, Region parent) {
		this.sggCode = sggCode;
		this.emdCode = emdCode;
		this.level = level;
		this.regionName = regionName;
		this.fullRegionName = fullRegionName;
		this.longitude = longitude;
		this.latitude = latitude;
		this.parent = parent;
	}

	public static Region create(
		String lawdCode,
		String fullName,
		RegionLevel level,
		Double longitude,
		Double latitude,
		Region parent
	) {
		if (lawdCode == null || lawdCode.length() != 10) {
			throw new IllegalArgumentException("유효하지 않은 법정동 코드: " + lawdCode);
		}
		if (fullName == null || fullName.isBlank()) {
			throw new IllegalArgumentException("전체 지역 명칭이 비어 있습니다.");
		}
		if (longitude == null || latitude == null) {
			throw new IllegalArgumentException("좌표가 비어 있습니다.");
		}

		if (level == RegionLevel.SIDO && parent != null) {
			throw new IllegalArgumentException("시·도 레벨 Region의 parent는 null이어야 합니다.");
		}
		if (level != RegionLevel.SIDO && parent == null) {
			throw new IllegalArgumentException("시·군·구/읍·면·동 레벨 Region은 parent가 필요합니다.");
		}

		String sggCode = lawdCode.substring(0, 5);
		String emdCode = extractEmdCode(lawdCode, level);
		String regionName = extractName(fullName, level);

		return new Region(
			sggCode,
			emdCode,
			level,
			regionName,
			fullName,
			longitude,
			latitude,
			parent
		);
	}

	private static String extractName(String fullName, RegionLevel level) {
		String[] parts = fullName.trim().split("\\s+");

		return switch (level) {
			case SIDO -> parts[0];

			case SIGUNGU -> {
				if (parts.length < 2) {
					throw new IllegalArgumentException("시군구 전체 명칭 형식 오류: " + fullName);
				}
				yield String.join(" ",
					java.util.Arrays.copyOfRange(parts, 1, parts.length)
				);
			}

			case EUP_MYEON_DONG -> {
				if (parts.length < 2) {
					throw new IllegalArgumentException("읍면동 전체 명칭 형식 오류: " + fullName);
				}
				yield parts[parts.length - 1];
			}
		};
	}

	private static String extractEmdCode(String lawdCode, RegionLevel level) {
		if (level != RegionLevel.EUP_MYEON_DONG) {
			return null;
		}
		return lawdCode.substring(5, 10);
	}

}
