package com.home.application.detail;

import org.springframework.stereotype.Service;

import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.infrastructure.web.detail.dto.DetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DetailUseCase {
	private final ComplexRepository complexRepository;

	public DetailResponse findById(Long id) {
		Complex detail = complexRepository.findById(id)
			.orElseThrow(RuntimeException::new); // todo 예외처리

		return DetailResponse.from(detail);
	}

}
