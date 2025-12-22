package com.home.infrastructure.batch.complex;

import java.time.LocalDate;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.home.domain.complex.Complex;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelService;
import com.home.infrastructure.batch.complex.dto.ComplexRowRequest;
import com.home.infrastructure.external.apis.ApisClient;
import com.home.infrastructure.external.apis.dto.ApisBldRecapDto;
import com.home.infrastructure.external.apis.dto.ApisBldRecapResponse;
import com.home.infrastructure.external.apis.dto.ApisRecapResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class ComplexItemProcessor implements ItemProcessor<ComplexRowRequest, Complex> {
	private final ApisClient apisClient;
	private final ParcelService parcelService;

	public ComplexItemProcessor(ApisClient apisClient, ParcelService parcelService) {
		this.apisClient = apisClient;
		this.parcelService = parcelService;
	}

	@Override
	public Complex process(@NonNull ComplexRowRequest dto) {

		if (!"1".equals(dto.gbCd()))
			return null;

		String normalizedPnu = normalizePnu(dto.pnu(), dto.address());

		Parcel parcel = parcelService.findByPnu(normalizedPnu).orElse(null);
		if (parcel == null) {
			log.warn("Parcel not found for pnu={}, address={}, tradeName={}",
				normalizedPnu, dto.address(), dto.tradeName());
			return null;
		}

		// 기존 로직: 동이 1개면 표제부, 2개 이상이면 총괄표제부
		ApisBldRecapDto bldDto = (dto.dongCnt() == 1)
			? getRecap(normalizedPnu)
			: getBldRecap(normalizedPnu);

		Double platArea = null;
		Double archArea = null;
		Double totArea = null;
		Double bcRat = null;
		Double vlRat = null;

		if (bldDto != null) {
			platArea = bldDto.platArea();
			archArea = bldDto.archArea();
			totArea = bldDto.totArea();
			bcRat = bldDto.bcRat();
			vlRat = bldDto.vlRat();
		}

		LocalDate useDate = (dto.useApt_dt() == null || dto.useApt_dt().isBlank())
			? null
			: LocalDate.parse(dto.useApt_dt().trim());

		return Complex.create(
			dto.complexPk(),
			normalizedPnu,
			dto.tradeName(),
			dto.conName(),
			dto.dongCnt(),
			dto.unitCnt(),
			platArea,
			archArea,
			totArea,
			bcRat,
			vlRat,
			useDate,
			parcel
		);
	}

	/**
	 * 총괄표제부 데이터 조회
	 * 총괄표제부 정의: 하나의 대지 안에 여러 동의 건축물이 있을 경우,
	 *            그 건축물 전체의 기본 사항을 종합적으로 기록한 문서
	 * @return ApisBldRecapDto 리스트가 1개일 때만 저장 (02000 필터 후)
	 * */
	private ApisBldRecapDto getBldRecap(String pnu) {
		if (pnu == null || pnu.length() < 19)
			return null;

		String sigunguCd = pnu.substring(0, 5);
		String bjdongCd = pnu.substring(5, 10);
		String bun = pnu.substring(11, 15);
		String ji = pnu.substring(15, 19);
		ApisBldRecapResponse bldRecap = apisClient.getBldRecap(
			sigunguCd,
			bjdongCd,
			bun,
			ji
		);
		if (bldRecap == null) {
			return null;
		}

		//toDto - "02000" 확인
		List<ApisBldRecapDto> list = bldRecap.toApisDto();
		if (list.size() != 1) {
			return null;
		}

		return list.get(0);
	}

	/**
	 * 표제부 데이터 조회
	 * 표제부 정의: 전국 자치단체의 건축행정정보시스템를 통해 생성된 건축물대장
	 * @return ApisBldRecapDto 리스트가 1개일 때만 저장 (02000 필터 후)
	 * */
	private ApisBldRecapDto getRecap(String pnu) {
		if (pnu == null || pnu.length() < 19)
			return null;

		String sigunguCd = pnu.substring(0, 5);
		String bjdongCd = pnu.substring(5, 10);
		String bun = pnu.substring(11, 15);
		String ji = pnu.substring(15, 19);
		ApisRecapResponse bldRecap = apisClient.getRecap(
			sigunguCd,
			bjdongCd,
			bun,
			ji
		);
		if (bldRecap == null) {
			return null;
		}

		List<ApisBldRecapDto> list = bldRecap.toApisDto();
		if (list.size() != 1) {
			return null;
		}

		return list.get(0);
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
