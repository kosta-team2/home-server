package com.home.infrastructure.web.detail.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TradeListResponse(
	Long tradeId,
	LocalDate dealDate,
	Double exclArea,
	Long dealAmount,
	String aptDong,
	Integer floor
) {

}
