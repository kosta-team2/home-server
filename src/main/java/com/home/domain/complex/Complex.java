package com.home.domain.complex;

import java.time.LocalDate;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;
import com.home.domain.parcel.Parcel;

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
@Table(name = "complex")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "complex_seq",
	sequenceName = "complex_id_seq",
	initialValue = 1,
	allocationSize = 100
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Complex extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "complex_seq")
	private Long id;

	/** 단지일련번호 */
	@Column(name = "apt_seq", unique = true)
	private String aptSeq;

	/** 단지고유번호 */
	@Column(name = "complex_pk", nullable = false, unique = true)
	private String complexPk;

	/** 필자고유번호 */
	@Column(name = "pnu", length = 19)
	private String pnu;

	/** 단지명_공시가격 */
	@Column(name = "trade_name", nullable = false)
	private String tradeName;

	/** 단지명_건축물대 */
	@Column(name = "name", nullable = false)
	private String name;

	/** 동수 */
	@Column(name = "dong_cnt")
	private Integer dongCnt;

	/** 세대수 */
	@Column(name = "unit_cnt")
	private Integer unitCnt;

	/** 대지면적 */
	@Column(name = "plat_area")
	private Double platArea;

	/** 건축면적 */
	@Column(name = "arch_area")
	private Double archArea;

	/** 연면적 */
	@Column(name = "tot_area")
	private Double totArea;

	/** 건폐율(%) */
	@Column(name = "bc_rat")
	private Double bcRat;

	/** 용적률(%) */
	@Column(name = "vl_rat")
	private Double vlRat;

	/** 사용승인일 */
	@Column(name = "use_date")
	private LocalDate useDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parcel_id")
	private Parcel parcel;

	private Complex(
		String aptSeq,
		String complexPk,
		String pnu,
		String tradeName,
		String name,
		Integer dongCnt,
		Integer unitCnt,
		Double platArea,
		Double archArea,
		Double totArea,
		Double bcRat,
		Double vlRat,
		LocalDate useDate,
		Parcel parcel
	) {
		this.aptSeq = aptSeq;
		this.complexPk = complexPk;
		this.pnu = pnu;
		this.tradeName = tradeName;
		this.name = name;
		this.dongCnt = dongCnt;
		this.unitCnt = unitCnt;
		this.platArea = platArea;
		this.archArea = archArea;
		this.totArea = totArea;
		this.bcRat = bcRat;
		this.vlRat = vlRat;
		this.useDate = useDate;
		this.parcel = parcel;
	}

	public static Complex create(
		String complexPk,
		String pnu,
		String tradeName,
		String name,
		Integer dongCnt,
		Integer unitCnt,
		Double platArea,
		Double archArea,
		Double totArea,
		Double bcRat,
		Double vlRat,
		LocalDate useDate,
		Parcel parcel
	) {
		if (tradeName == null || tradeName.isEmpty()) {
			tradeName = name;
		}

		if (name == null || name.isEmpty()) {
			name = tradeName;
		}

		return new Complex(null, complexPk, pnu, tradeName, name,
			dongCnt, unitCnt, platArea, archArea, totArea, bcRat, vlRat, useDate, parcel);
	}

	/**
	 * 아파트 단지를 실거래가와 연결할 aptSeq를 설정할 때 사용
	 * */
	public void updateAptSeq(String newSeq) {
		if (newSeq == null || newSeq.isBlank()) {
			throw new IllegalArgumentException("새 aptSeq 값이 비어 있습니다.");
		}

		if (this.aptSeq == null || this.aptSeq.isBlank()) {
			this.aptSeq = newSeq;
			return;
		}

		//같은 값이면 무시
		if (this.aptSeq.equals(newSeq)) {
			return;
		}

		//TODO: 에러처리 개선
		//위 조건을 통과하지 못하면 에러로 판단.
		// throw new IllegalStateException(
		// 	"aptSeq 값이 이미 존재합니다. 기존=" + this.aptSeq + ", 새=" + newSeq
		// );
	}
}
