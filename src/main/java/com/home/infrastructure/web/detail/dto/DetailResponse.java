package com.home.infrastructure.web.detail.dto;

import com.home.domain.complex.Complex;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DetailResponse {
	private Long id;
	private String address;		// 주소
	private String tradeName;	// 아파트 이름
	private String name;		// 단지명
	private Double latitude;
	private Double longitude;
	private Integer dongCnt;	// 동수
	private Integer unitCnt;	// 세대수
	private Double platArea;	// 대지 면적
	private Double archArea;	// 건축 면적
	private Double totArea;		// 연면적
	private Double bcRat;		// 건폐율
	private Double vlRat;		// 용적률
	private Integer buildYear;	// 건축년도

	public static DetailResponse from(Complex complex) {
		return new DetailResponse(
			complex.getId(),
			complex.getAddress(),
			complex.getTradeName(),
			complex.getName(),
			complex.getLatitude(),
			complex.getLongitude(),
			complex.getDongCnt(),
			complex.getUnitCnt(),
			complex.getPlatArea(),
			complex.getArchArea(),
			complex.getTotArea(),
			complex.getBcRat(),
			complex.getVlRat(),
			complex.getBuildYear()
		);
	}

}
