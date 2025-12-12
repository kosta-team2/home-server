package com.home.application.detail;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.infrastructure.web.detail.dto.DetailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DetailUseCase {
	private final ParcelRepository parcelRepository;
	private final ComplexRepository complexRepository;

	@Transactional(readOnly = true)
	public DetailResponse findByParcelId(Long id) {
		Parcel parcel = parcelRepository.findById(id)
			.orElseThrow(RuntimeException::new);	// todo 예외처리 not found

		List<Complex> complexes = complexRepository.findAllByParcel_Id(id);
		if (complexes.isEmpty()) throw new RuntimeException();	// todo 예외처리 not found

		// 1개의 parcel에 여러 complex가 존재시 complex를 제외하고 보낸다.
		if (complexes.size() != 1) return DetailResponse.from(parcel);
		return DetailResponse.of(parcel, complexes.get(0));
	}

}
