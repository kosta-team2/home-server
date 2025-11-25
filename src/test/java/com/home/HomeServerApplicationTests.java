package com.home;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
class HomeServerApplicationTests {

	@Test
	void contextLoads() {
	}

	/*
	 * Testcontainers: 테스트 전용 DB
	 * 같은 종류의 DB를, 완전 분리된 인스턴스로 매번 띄워주는 도구
	 * */
	@TestConfiguration(proxyBeanMethods = false)
	static class ContainersConfig {

		@Bean
		@ServiceConnection
		PostgreSQLContainer<?> postgresContainer() {
			return new PostgreSQLContainer<>("postgres:16");
		}
	}

}
