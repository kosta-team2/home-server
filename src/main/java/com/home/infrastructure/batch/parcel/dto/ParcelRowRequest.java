package com.home.infrastructure.batch.parcel.dto;

public record ParcelRowRequest(
	String pnu,
	String address,
	String gbCd
) {
}
