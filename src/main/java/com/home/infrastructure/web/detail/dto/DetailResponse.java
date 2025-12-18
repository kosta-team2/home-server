package com.home.infrastructure.web.detail.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.home.domain.complex.Complex;
import com.home.domain.parcel.Parcel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetailResponse(
	Long parcelId,
	Double latitude,
	Double longitude,
	String address,
	String tradeName,   // 아파트 이름
	String name,        // 단지명
	Integer dongCnt,    // 동수
	Integer unitCnt,    // 세대수
	Double platArea,    // 대지 면적
	Double archArea,    // 건축 면적
	Double totArea,     // 연면적
	Double bcRat,       // 건폐율
	Double vlRat,       // 용적률
	LocalDate useDate   // 사용승인일
) {
	public static DetailResponse of(Parcel parcel, Complex complex) {
		return new DetailResponse(
			parcel.getId(),
			parcel.getLatitude(),
			parcel.getLongitude(),
			parcel.getAddress(),
			complex.getTradeName(),
			complex.getName(),
			complex.getDongCnt(),
			complex.getUnitCnt(),
			complex.getPlatArea(),
			complex.getArchArea(),
			complex.getTotArea(),
			complex.getBcRat(),
			complex.getVlRat(),
			complex.getUseDate()
		);
	}

}
