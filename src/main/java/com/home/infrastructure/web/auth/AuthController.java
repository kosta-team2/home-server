package com.home.infrastructure.web.auth;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.application.auth.AuthUseCase;
import com.home.infrastructure.web.auth.dto.AccessTokenResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthUseCase authUseCase;

	@PostMapping("/access")
	public AccessTokenResponse issueAccessToken(@CookieValue(name="refresh_token", required=false) String rt) {
		return new AccessTokenResponse(authUseCase.issueAccessToken(rt));
	}
}
