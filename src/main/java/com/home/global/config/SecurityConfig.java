package com.home.global.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.home.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.home.infrastructure.security.jwt.JwtProperties;
import com.home.infrastructure.security.oauth.CustomOAuth2UserService;
import com.home.infrastructure.security.oauth.OAuth2LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	private final CustomOAuth2UserService oAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			//csrf off
			.csrf(AbstractHttpConfigurer::disable)

			//session off
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			//cors 적용
			.cors(cors -> cors.configurationSource(corsConfiguration()))

			//jwt 세팅
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

			// 모든 접근 허용
			.authorizeHttpRequests(auth ->
				auth.anyRequest().permitAll())

			.oauth2Login(oauth -> oauth
				.userInfoEndpoint(u -> u.userService(oAuth2UserService))
				.successHandler(oAuth2LoginSuccessHandler))

		;

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfiguration() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(
			List.of("http://localhost:5173"));

		config.addAllowedHeader(
			"*"); // corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
		config.addAllowedMethod(
			"*"); // corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));

		//쿠키 허용
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// 모든 경로에 대해서 CORS 설정을 적용
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
