package com.home.infrastructure.batch.parcel;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelService;
import com.home.infrastructure.batch.parcel.dto.ParcelRowRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class ParcelItemProcessor implements ItemProcessor<ParcelRowRequest, Parcel> {
	private final ParcelService parcelService;

	public ParcelItemProcessor(ParcelService parcelService) {
		this.parcelService = parcelService;
	}

	@Override
	public Parcel process(ParcelRowRequest item) {
		if (!"1".equals(item.gbCd()))
			return null;

		String normalizedPnu = normalizePnu(item.pnu(), item.address());

		return parcelService.findRawByPnuAndAddress(normalizedPnu, item.address());
	}

	/**
	 * csv 파일 문제로 인한 pnu 재정의
	 * TODO: pnu의 정의가 맞지 않아서 지번을 통한 정의를 진행 -> 이유를 파악하지 못해서 후에 api로 변경 고려
	 * */
	private String normalizePnu(String originalPnu, String address) {
		if (originalPnu == null || originalPnu.length() < 11 || address == null) {
			return originalPnu;
		}

		String prefix11 = originalPnu.substring(0, 11);
		String[] parts = address.trim().split("\\s+");
		if (parts.length == 0) {
			return originalPnu;
		}
		String lot = parts[parts.length - 1];

		String mainStr;
		String subStr;

		if (lot.contains("-")) {
			String[] lotParts = lot.split("-");
			mainStr = lotParts[0];
			subStr = lotParts.length > 1 ? lotParts[1] : "0";
		} else {
			mainStr = lot;
			subStr = "0";
		}

		mainStr = mainStr.replaceAll("\\D", "");
		subStr = subStr.replaceAll("\\D", "");

		if (mainStr.isEmpty()) {
			log.warn("normalizePnu main empty. originalPnu={}, address={}", originalPnu, address);
			return originalPnu;
		}

		String main4 = String.format("%04d", Integer.parseInt(mainStr));      // 본번
		String sub4 = String.format("%04d", subStr.isEmpty() ? 0 : Integer.parseInt(subStr)); // 부번

		String normalized = prefix11 + main4 + sub4;

		if (normalized.length() != 19) {
			log.warn("normalizePnu length invalid. originalPnu={}, address={}, normalized={}",
				originalPnu, address, normalized);
			return originalPnu;
		}

		return normalized;
	}
}
