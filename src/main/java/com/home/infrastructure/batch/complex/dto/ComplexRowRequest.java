package com.home.infrastructure.batch.complex.dto;

public record ComplexRowRequest(
	String complexPk,
	String pnu,
	String address,
	String tradeName,
	String conName,
	String roadName,
	String gbCd,
	int dongCnt,
	int unitCnt,
	String useApt_dt
) {
}
