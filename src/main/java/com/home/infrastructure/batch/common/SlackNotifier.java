package com.home.infrastructure.batch.common;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlackNotifier {

	private final RestClient restClient;
	private final String webhookUrl;

	public SlackNotifier(
		RestClient.Builder builder,
		@Value("${slack.webhook-url:}") String webhookUrl
	) {
		this.restClient = builder.build();
		this.webhookUrl = webhookUrl;
	}

	public void send(String message) {
		if (webhookUrl == null || webhookUrl.isBlank()) {
			log.warn("[BATCH][SLACK] webhook-url not set. skip.");
			return;
		}

		try {
			restClient.post()
				.uri(webhookUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("text", message))
				.retrieve()
				.toBodilessEntity();

			log.info("[BATCH][SLACK] sent");
		} catch (Exception e) {
			log.warn("[BATCH][SLACK] send failed: {}", e.getMessage());
		}
	}
}
