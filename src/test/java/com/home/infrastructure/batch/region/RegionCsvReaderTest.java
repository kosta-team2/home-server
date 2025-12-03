package com.home.infrastructure.batch.region;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.home.infrastructure.batch.region.dto.RegionCsvRowResponse;

class RegionCsvReaderTest {

	@TempDir
	Path tempDir;

	private static final String CONTENT = """
		법정동코드,법정동명,상태
		1114015000,서울특별시 중구 을지로4가,존재
		1114016000,서울특별시 중구 을지로5가,폐지
		1114017000,서울특별시 중구 을지로6가,존재
		4122025033,경기도 평택시 팽성읍 함정리,존재
		""";

	@Test
	@DisplayName("CSV에서 상태가 '존재'인 행만 읽어온다")
	void readExisting_success() throws IOException {
		// given
		Path csv = tempDir.resolve("lawd.csv");

		Files.writeString(csv, CONTENT);

		RegionCsvReader reader = new RegionCsvReader();

		// when
		List<RegionCsvRowResponse> rows = reader.readExisting(csv);

		// then
		assertThat(rows).hasSize(2);

		RegionCsvRowResponse first = rows.get(0);
		assertThat(first.lawdCode()).isEqualTo("1114015000");
		assertThat(first.fullName()).isEqualTo("서울특별시 중구 을지로4가");

		RegionCsvRowResponse second = rows.get(1);
		assertThat(second.lawdCode()).isEqualTo("1114017000");
		assertThat(second.fullName()).isEqualTo("서울특별시 중구 을지로6가");
	}
}
