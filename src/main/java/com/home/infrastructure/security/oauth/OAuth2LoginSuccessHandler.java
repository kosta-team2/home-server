package com.home.infrastructure.security.oauth;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.home.infrastructure.security.jwt.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final RefreshTokenService refreshTokenService;

	@Value("${domain.frontend:http://localhost:5173}")
	private String frontendUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		UserDetailPrincipal principal = (UserDetailPrincipal)authentication.getPrincipal();
		Long userId = principal.getUserId();

		String refreshRaw = refreshTokenService.issue(userId, Duration.ofHours(24));

		ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshRaw)
			.httpOnly(true)
			.secure(false) //TODO: 나중에 실제 환경에서 true로 승격
			.sameSite("Lax")
			.path("/auth")
			.maxAge(Duration.ofHours(24))
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth/callback");
	}
}
