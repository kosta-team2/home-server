package com.home.domain.trade;

import java.time.LocalDate;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;
import com.home.domain.complex.Complex;

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
@Table(name = "trade")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "trade_seq",
	sequenceName = "trade_id_seq",
	initialValue = 1,
	allocationSize = 100
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_seq")
	private Long id;

	/** 아파트 동명(거의 안나옴) ex)208 */
	@Column(name = "apt_dong")
	private String aptDong;

	/** 거래금액 */
	@Column(name = "deal_amount", nullable = false)
	private Long dealAmount;

	/** 거래날짜 */
	@Column(name = "deal_date", nullable = false)
	private LocalDate dealDate;

	/** 층 */
	@Column(name = "floor")
	private Integer floor;

	/** 전용면적(m²) */
	@Column(name = "excl_area")
	private Double exclArea;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "complex_id", nullable = false)
	private Complex complex;

	private Trade(String aptDong, Long dealAmount, LocalDate dealDate, Integer floor, Double exclArea,
		Complex complex) {
		this.aptDong = aptDong;
		this.dealAmount = dealAmount;
		this.dealDate = dealDate;
		this.floor = floor;
		this.exclArea = exclArea;
		this.complex = complex;
	}

	public static Trade create(
		String aptDong,
		String dealAmount,
		Integer dealDay,
		Integer dealMonth,
		Integer dealYear,
		Integer floor,
		Double excluUseAr,
		Complex complex
	) {
		if (dealAmount == null || dealAmount.equals(" ")) {
			throw new IllegalArgumentException("유효하지 않은 거래 금액: " + dealAmount);
		}
		if (dealYear == null || dealMonth == null || dealDay == null) {
			throw new IllegalArgumentException(
				"유효하지 않은 거래 일자: " + dealYear + "-" + dealMonth + "-" + dealDay
			);
		}
		if (complex == null) {
			throw new IllegalArgumentException("Trade 생성 시 Complex는 필수입니다.");
		}

		// 0층은 사실상 정보 없음으로 처리
		if (floor != null && floor == 0) {
			floor = null;
		}

		LocalDate dealDate = LocalDate.of(dealYear, dealMonth, dealDay);
		Long parsedAmount = parseDealAmount(dealAmount);

		return new Trade(
			aptDong,
			parsedAmount,
			dealDate,
			floor,
			excluUseAr,
			complex
		);
	}

	private static Long parseDealAmount(String rawAmount) {
		if (rawAmount == null) {
			throw new IllegalArgumentException("거래금액(dealAmount)이 null 입니다.");
		}

		String cleaned = rawAmount.replace(",", "").trim();

		return Long.parseLong(cleaned);
	}
}
