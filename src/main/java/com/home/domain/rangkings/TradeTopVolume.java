package com.home.domain.rangkings;

import org.hibernate.annotations.Immutable;

import com.home.domain.complex.Complex;
import com.home.domain.region.Region;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "trade_top_volume_30d")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable // Hibernate: 읽기 전용 (insert/update 방지)
@IdClass(TradeTopVolumeId.class)
public class TradeTopVolume{
	@Id
	@Column(name = "region_id", nullable = false)
	private Long regionId;

	@Id
	@Column(name = "rank", nullable = false)
	private Integer rank;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id", insertable = false, updatable = false)
	private Region region;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "complex_id", nullable = false)
	private Complex complex;

	@Column(name = "deal_count", nullable = false)
	private Integer dealCount;
}
