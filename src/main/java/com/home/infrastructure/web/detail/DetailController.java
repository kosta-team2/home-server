package com.home.infrastructure.web.detail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.home.application.detail.DetailUseCase;
import com.home.infrastructure.web.detail.dto.DetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/detail")
public class DetailController {
	private final DetailUseCase detailUseCase;

	@GetMapping("/{id}")
	public ResponseEntity<DetailResponse> getDetailById(@PathVariable Long id) {
		DetailResponse response = detailUseCase.findByParcelId(id);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
