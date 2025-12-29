package com.home.infrastructure.batch.trade.dto;

import java.time.LocalDate;

public record TradeRow(
	LocalDate dealDate,
	String aptDong,
	long dealAmount,
	Integer floor,
	Double exclArea,
	String complexPk,
	String aptSeq,
	String source,
	String sourceKey
) {
}
