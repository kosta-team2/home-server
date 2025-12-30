package com.home.domain.rangkings;

import org.hibernate.annotations.Immutable;

import com.home.domain.complex.Complex;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "trade_top_price_30d")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable // Hibernate: 읽기 전용
public class TradeTopPrice {
	@Id
	@Column(name = "rank", nullable = false)
	private Integer rank;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "complex_id", nullable = false)
	private Complex complex;

	@Column(name = "max_price", nullable = false)
	private Long maxPrice;
}
