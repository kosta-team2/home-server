package com.home.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.home.global.exception.ErrorCode;

class ErrorCodeDocsTest {

	private static final Path SNIPPETS_DIR = Path.of("build/generated-snippets");

	@Test
	void generate_error_code_sections() throws IOException {
		Files.createDirectories(SNIPPETS_DIR);

		Path file = SNIPPETS_DIR.resolve("error-codes-sections.adoc");

		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			for (ErrorCode errorCode : ErrorCode.values()) {
				writeSection(writer, errorCode);
			}
		}

		assertThat(Files.exists(file)).isTrue();
	}

	private void writeSection(BufferedWriter writer, ErrorCode errorCode) throws IOException {
		String anchor = errorCode.getTitle();
		HttpStatus status = errorCode.getHttpStatus();
		String detail = errorCode.getDetail();

		writer.write("[[" + anchor + "]]");
		writer.newLine();
		writer.write("=== " + anchor);
		writer.newLine();
		writer.newLine();

		writer.write("*HTTP 상태*:: `" + status.value() + " " + status.getReasonPhrase() + "`");
		writer.newLine();
		writer.newLine();

		writer.write("*detail(기본 메시지)*:: `" + detail + "`");
		writer.newLine();
		writer.newLine();
	}
}
