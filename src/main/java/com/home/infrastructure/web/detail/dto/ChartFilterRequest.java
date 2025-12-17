package com.home.infrastructure.web.detail.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ChartFilterRequest (
	@NotNull(message = "start date 값은 필수입니다.")
	LocalDate startDate,

	@NotNull(message = "end date 값은 필수입니다.")
	LocalDate endDate,

	@NotNull(message = "exclArea 값은 필수입니다.")
	Double exclArea
){

}
