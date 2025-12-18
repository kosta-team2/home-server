package com.home.infrastructure.web.detail.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.home.domain.complex.Complex;
import com.home.domain.parcel.Parcel;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.InvalidParameterException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailResponse {

	private Long parcelId;
	private Double latitude;
	private Double longitude;
	private String address;     // 주소
	private String tradeName;   // 아파트 이름
	private String name;        // 단지명
	private Integer dongCnt;    // 동수
	private Integer unitCnt;    // 세대수
	private Double platArea;    // 대지 면적
	private Double archArea;    // 건축 면적
	private Double totArea;     // 연면적
	private Double bcRat;       // 건폐율
	private Double vlRat;       // 용적률
	private LocalDate useDate;  // 사용승인일/건축년도

	public static DetailResponse from(Parcel parcel) {
		if (parcel == null) {
			throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER);
			// log.warn();
		}
		return DetailResponse.builder()
			.parcelId(parcel.getId())
			.latitude(parcel.getLatitude())
			.longitude(parcel.getLongitude())
			.address(parcel.getAddress())
			.build();
	}

	public static DetailResponse of(Parcel parcel, Complex complex) {
		if (parcel == null || complex == null) {
			return from(parcel);
		}

		return DetailResponse.builder()
			.parcelId(parcel.getId())
			.latitude(parcel.getLatitude())
			.longitude(parcel.getLongitude())
			.address(parcel.getAddress())
			.tradeName(complex.getTradeName())
			.name(complex.getName())
			.dongCnt(complex.getDongCnt())
			.unitCnt(complex.getUnitCnt())
			.platArea(complex.getPlatArea())
			.archArea(complex.getArchArea())
			.totArea(complex.getTotArea())
			.bcRat(complex.getBcRat())
			.vlRat(complex.getVlRat())
			.useDate(complex.getUseDate())
			.build();
	}

}
