package com.home.domain.complex;

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
@Table(name = "complex")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "complex_seq",
	sequenceName = "complex_id_seq",
	initialValue = 1,
	allocationSize = 50
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
	@Column(name = "pnu", nullable = false)
	private String pnu;

	/** 주소(대지위치) ex) 경기도 고양시 일산동구 백석동 1183번지 */
	@Column(name = "address", nullable = false)
	private String address;

	/** 단지명_공시가격 */
	@Column(name = "trade_name", nullable = false)
	private String tradeName;

	/** 단지명_건축물대 */
	@Column(name = "name", nullable = false)
	private String name;

	/** 단지 대표 경도 (x, EPSG:4326 기준) */
	@Column(name = "longitude")
	private Double longitude;

	/** 단지 대표 위도 (y, EPSG:4326 기준) */
	@Column(name = "latitude")
	private Double latitude;

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

	/** 건축년도 */
	@Column(name = "build_year")
	private Integer buildYear;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id", nullable = false)
	private Region region;

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

		//위 조건을 통과하지 못하면 에러로 판단.
		throw new IllegalStateException(
			"aptSeq 값이 이미 존재합니다. 기존=" + this.aptSeq + ", 새=" + newSeq
		);
	}
}
