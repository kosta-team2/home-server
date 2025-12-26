package com.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomeServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeServerApplication.class, args);
	}

}
