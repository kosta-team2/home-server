package com.home.infrastructure.batch.region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.home.infrastructure.batch.region.dto.RegionCsvRowRequest;
import com.home.infrastructure.batch.region.dto.RegionCsvRowResponse;

/**
 * 법정동 코드 CSV를 읽어서 RegionCsvRow 리스트로 변환하는 리더
 * - 기본적으로 상태(status)가 "존재"인 행만 필터링한다.
 */
public class RegionCsvReader {
	private static final String EXIST_STATUS = "존재";

	public List<RegionCsvRowResponse> readExisting(Path csvPath) {
		return readExisting(csvPath, StandardCharsets.UTF_8);
	}

	public List<RegionCsvRowResponse> readExisting(Path csvPath, Charset charset) {
		List<RegionCsvRowResponse> result = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(csvPath, charset)) {
			String line;

			//첫 줄 헤더 무조건 스킵
			reader.readLine();

			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}

				RegionCsvRowRequest raw = parseLine(line);

				if (EXIST_STATUS.equals(raw.status())) {
					result.add(new RegionCsvRowResponse(raw.lawdCode(), raw.fullName()));
				}
			}

			return result;
		} catch (IOException e) {
			throw new UncheckedIOException("법정동 코드 CSV를 읽는 중 오류 발생: " + csvPath, e);
		}
	}

	private RegionCsvRowRequest parseLine(String line) {
		String[] tokens = line.split(",", -1);

		if (tokens.length < 3) {
			throw new IllegalArgumentException("잘못된 CSV 형식: " + line);
		}

		String lawdCode = tokens[0].trim();
		String fullName = tokens[1].trim();
		String status = tokens[2].trim();

		return new RegionCsvRowRequest(lawdCode, fullName, status);
	}

}
