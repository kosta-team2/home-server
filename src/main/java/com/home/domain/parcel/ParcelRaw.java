package com.home.domain.parcel;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(schema = "raw", name = "parcel_raw")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable // Hibernate: 읽기 전용 (insert/update 방지)
public class ParcelRaw {
	@Id
	@Column(name = "pnu", length = 19)
	private String pnu;

	/** 단지 대표 경도 (x, EPSG:4326 기준) */
	@Column(name = "longitude")
	private Double longitude;

	/** 단지 대표 위도 (y, EPSG:4326 기준) */
	@Column(name = "latitude")
	private Double latitude;
}
