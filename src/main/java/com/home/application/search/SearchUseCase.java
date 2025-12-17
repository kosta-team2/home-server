package com.home.application.search;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.home.domain.complex.ComplexRepository;
import com.home.infrastructure.web.search.dto.ComplexSearchResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchUseCase {
	private final ComplexRepository complexRepository;

	public List<ComplexSearchResponse> searchComplexes(String q) {
		String keyword = q == null ? "" : q.trim();
		if (keyword.isEmpty()) return List.of();

		return complexRepository.searchComplexesWithParcel(keyword, PageRequest.of(0, 10));
	}
}
