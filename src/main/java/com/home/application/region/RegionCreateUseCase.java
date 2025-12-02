package com.home.application.region;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.region.Region;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.global.exception.external.ExternalApiException;
import com.home.infrastructure.batch.region.dto.RegionCsvRowResponse;
import com.home.infrastructure.external.vworld.VworldClient;
import com.home.infrastructure.external.vworld.dto.VworldAreaCoordinateResponse;
import com.home.infrastructure.external.vworld.dto.VworldAreaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionCreateUseCase {

	private final RegionRepository regionRepository;
	private final VworldClient vworldClient;

	/**
	 * CSV에서 읽어온 "존재" 상태의 법정동 코드 리스트를 기반으로
	 * Region 엔티티들을 생성/저장한다.
	 */
	@Transactional
	public void importRegions(List<RegionCsvRowResponse> rows) {
		for (RegionCsvRowResponse row : rows) {
			try {
				Region region = createRegion(row);
				regionRepository.save(region);
			} catch (ExternalApiException e) {
				log.warn("VWorld 행정구역 조회 실패, lawdCode={}, fullName={}, reason={}",
					row.lawdCode(), row.fullName(), e.getMessage());
			}
		}
	}

	private Region createRegion(RegionCsvRowResponse row) {
		String lawdCode = row.lawdCode();
		String fullName = row.fullName();

		RegionLevel level = RegionLevel.from(lawdCode);
		Region parent = findParent(fullName, level);

		String dataSet;
		String attrFilter;

		switch (level) {
			case SIDO -> {
				String ctprvnCd = lawdCode.substring(0, 2);
				dataSet = "LT_C_ADSIDO_INFO";
				attrFilter = "ctprvn_cd:=:" + ctprvnCd;
			}
			case SIGUNGU -> {
				String sigCd = lawdCode.substring(0, 5);
				dataSet = "LT_C_ADSIGG_INFO";
				attrFilter = "sig_cd:=:" + sigCd;
			}
			case EUP_MYEON_DONG -> {
				String emdCd = lawdCode.substring(0, 8);
				dataSet = "LT_C_ADEMD_INFO";
				attrFilter = "emd_cd:=:" + emdCd;
			}
			default -> throw new IllegalStateException("지원하지 않는 RegionLevel: " + level);
		}

		VworldAreaResponse areaResponse = vworldClient.getAdminArea(dataSet, attrFilter);
		VworldAreaCoordinateResponse response = areaResponse.toCoordinate();

		return Region.create(
			lawdCode,
			fullName,
			level,
			response.longitude(),
			response.latitude(),
			parent
		);

	}

	private Region findParent(String fullName, RegionLevel level) {
		if (level == RegionLevel.SIDO) {
			return null;
		}

		String[] parts = fullName.trim().split("\\s+");
		String parentName;

		switch (level) {
			case SIGUNGU -> parentName = parts[0];
			case EUP_MYEON_DONG -> {
				if (parts.length < 3) {
					throw new IllegalArgumentException("읍·면·동 레벨인데 상위 행정구역 정보가 부족합니다. fullName=" + fullName);
				}
				parentName = String.join(" ",
					Arrays.copyOf(parts, parts.length - 1)
				);
			}
			default -> throw new IllegalStateException("지원하지 않는 RegionLevel 입니다: " + level);
		}

		return regionRepository.findByFullRegionName(parentName)
			.orElseThrow(() -> new IllegalStateException(
				"상위 행정구역 Region이 존재하지 않습니다. parent=" + parentName)
			);
	}

}
