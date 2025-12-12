package com.home.domain.parcel;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;
import com.home.domain.region.Region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "parcel")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "parcel_seq",
	sequenceName = "parcel_id_seq",
	initialValue = 1,
	allocationSize = 50
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parcel extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parcel_seq")
	private Long id;

	/** 필자고유번호 */
	@Column(name = "pnu", length = 19, nullable = false)
	private String pnu;

	/** 단지 대표 경도 (x, EPSG:4326 기준) */
	@Column(name = "longitude")
	private Double longitude;

	/** 단지 대표 위도 (y, EPSG:4326 기준) */
	@Column(name = "latitude")
	private Double latitude;

	/** 주소(대지위치) ex) 경기도 고양시 일산동구 백석동 1183 */
	@Column(name = "address", nullable = false)
	private String address;

	/** 읽기 전용 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id", insertable = false, updatable = false)
	private Region region;

	public Parcel(String pnu, Double longitude, Double latitude, String address) {
		this.pnu = pnu;
		this.longitude = longitude;
		this.latitude = latitude;
		this.address = address;
	}
}
