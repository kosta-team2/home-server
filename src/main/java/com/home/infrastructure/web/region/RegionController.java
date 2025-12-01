package com.home.infrastructure.web.region;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.region.RegionCreateUseCase;
import com.home.infrastructure.batch.region.RegionCsvReader;
import com.home.infrastructure.batch.region.dto.RegionCsvRowResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/region")
@Profile("local") //프로필: local 에서만 활성화
@RequiredArgsConstructor
public class RegionController {
	private final RegionCsvReader regionCsvReader;
	private final RegionCreateUseCase regionCreateUseCase;

	@Value("${home.region.csv-path}")
	private String regionCsvPath;

	@PostMapping("/import")
	public ResponseEntity<?> importRegions() {
		Path path = Paths.get(regionCsvPath);

		List<RegionCsvRowResponse> rows = regionCsvReader.readExisting(path);

		regionCreateUseCase.importRegions(rows);

		return ResponseEntity.ok().build();
	}

}
