package com.home;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.home.config.TestContainersConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
class HomeServerApplicationTests {

	@Test
	void contextLoads() {
	}
}
