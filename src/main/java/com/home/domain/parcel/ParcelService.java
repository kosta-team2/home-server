package com.home.domain.parcel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParcelService {
	private final ParcelRepository parcelRepository;
	private final ParcelRawRepository parcelRawRepository;

	@Transactional
	public Parcel getOrCreateFromRaw(String pnu, String address) {
		return parcelRepository.findByPnu(pnu)
			.orElseGet(() -> {
				return parcelRawRepository.findById(pnu)
					.map(raw -> {
						Parcel parcel = new Parcel(
							pnu,
							raw.getLongitude(),
							raw.getLatitude(),
							address
						);
						return parcelRepository.save(parcel);
					})
					.orElse(null); // raw 없으면 null 리턴
			});
	}

}
