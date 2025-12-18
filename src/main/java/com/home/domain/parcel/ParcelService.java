package com.home.domain.parcel;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParcelService {
	private final ParcelRepository parcelRepository;
	private final ParcelRawRepository parcelRawRepository;

	@Transactional(readOnly = true)
	public Optional<Parcel> findByPnu(String pnu) {
		return parcelRepository.findByPnu(pnu);
	}

	@Transactional(readOnly = true)
	public Parcel findRawByPnuAndAddress(String pnu, String address) {
		if (parcelRepository.findByPnu(pnu).isPresent()) {
			return null;
		}

		return parcelRawRepository.findById(pnu)
			.map(raw -> new Parcel(pnu, raw.getLongitude(), raw.getLatitude(), address))
			.orElse(null);
	}

}
