package com.home.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeDocsTest {

	private static final Path SNIPPETS_DIR = Path.of("build/generated-snippets");

	@Test
	void generate_error_code_sections() throws IOException {
		Files.createDirectories(SNIPPETS_DIR);

		Path file = SNIPPETS_DIR.resolve("error-codes-sections.adoc");

		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			writeTableHeader(writer);

			for (ErrorCode errorCode : ErrorCode.values()) {
				writeTableRow(writer, errorCode);
			}

			writeTableFooter(writer);
		}

		assertThat(Files.exists(file)).isTrue();
	}

	private void writeTableHeader(BufferedWriter writer) throws IOException {
		writer.write("[cols=\"2,3,5\"]");
		writer.newLine();
		writer.write("|====");
		writer.newLine();
		writer.write("|코드 |HTTP 상태 |설명");
		writer.newLine();
	}

	private void writeTableRow(BufferedWriter writer, ErrorCode errorCode) throws IOException {
		String code = errorCode.getTitle();
		HttpStatus status = errorCode.getHttpStatus();
		String detail = errorCode.getDetail();

		writer.write("|[[" + code + "]]" + code);
		writer.newLine();

		writer.write("|`" + status.getReasonPhrase() + "(" + status.value() + ")" + "`");
		writer.newLine();

		writer.write("|" + detail);
		writer.newLine();
	}

	private void writeTableFooter(BufferedWriter writer) throws IOException {
		writer.write("|====");
		writer.newLine();
	}
}
