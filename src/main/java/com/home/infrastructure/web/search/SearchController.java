package com.home.infrastructure.web.search;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.home.application.search.SearchUseCase;
import com.home.infrastructure.web.search.dto.ComplexSearchResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchUseCase searchUseCase;

	@GetMapping("/complexes")
	public ResponseEntity<List<ComplexSearchResponse>> searchComplexes(@RequestParam("q") String q) {
		return ResponseEntity.ok(searchUseCase.searchComplexes(q));
	}
}
