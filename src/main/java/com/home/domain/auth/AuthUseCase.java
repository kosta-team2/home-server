package com.home.domain.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.user.User;
import com.home.domain.user.UserRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.UnauthorizedException;
import com.home.infrastructure.security.jwt.JwtTokenProvider;
import com.home.infrastructure.security.jwt.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUseCase {

	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional(readOnly = true)
	public String issueAccessToken(String refreshTokenRaw) {
		if (refreshTokenRaw == null || refreshTokenRaw.isBlank()) {
			throw new UnauthorizedException("refreshToken 값이 존재하지 않습니다.");
		}

		Long userId;
		try {
			userId = refreshTokenService.validate(refreshTokenRaw);
		} catch (Exception e) {
			// 여기서는 원인 숨기고 401로 통일
			throw new UnauthorizedException("refreshToken 검증에 실패하였습니다.");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));

		return jwtTokenProvider.createAccessToken(user);
	}
}
