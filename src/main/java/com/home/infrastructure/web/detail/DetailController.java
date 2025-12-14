package com.home.infrastructure.web.detail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.home.application.detail.DetailUseCase;
import com.home.infrastructure.web.detail.dto.ChartFilterRequest;
import com.home.infrastructure.web.detail.dto.DetailResponse;
import com.home.infrastructure.web.detail.dto.TradeResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/detail")
public class DetailController {
	private final DetailUseCase detailUseCase;

	@GetMapping("/{parcelId}")
	public ResponseEntity<DetailResponse> getDetailById(@PathVariable Long parcelId) {
		DetailResponse response = detailUseCase.findDetailByParcelId(parcelId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * 거래 목록 조회용
	 * @param parcelId
	 * @return
	 */
	@GetMapping("/trade/{parcelId}")
	public ResponseEntity<TradeResponse> getTradeById(@PathVariable Long parcelId) {
		TradeResponse response = detailUseCase.findAllTradeByParcelId(parcelId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 * 필터링 된 거래 목록 조회
	 * @param parcelId
	 * @param ChartFilterRequest
	 * @return
	 */
	@PostMapping("/trade/{parcelId}")
	public ResponseEntity<TradeResponse> getTradeChartById(
		@PathVariable Long parcelId,
		@Valid @RequestBody ChartFilterRequest ChartFilterRequest) {
		TradeResponse response = detailUseCase.findAllFilterdTradeByParcelId(parcelId, ChartFilterRequest);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
