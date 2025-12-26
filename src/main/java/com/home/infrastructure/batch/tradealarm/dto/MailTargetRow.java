package com.home.infrastructure.batch.tradealarm.dto;

public record MailTargetRow(
	Long id,
	Long userId,
	Long parcelId,
	String email,
	String complexName,
	int tryCount
) {
}
