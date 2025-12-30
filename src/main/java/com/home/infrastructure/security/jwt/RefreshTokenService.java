package com.home.infrastructure.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.user.RefreshToken;
import com.home.domain.user.RefreshTokenRepository;
import com.home.domain.user.User;
import com.home.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

	private static final SecureRandom RANDOM = new SecureRandom();

	@Transactional
	public String issue(Long userId, Duration ttl) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalStateException("User not found: " + userId));

		Instant now = Instant.now();

		int revoked = refreshTokenRepository.revokeActiveByUserId(userId, now);
		log.info("revoked active refresh tokens: {}", revoked);

		String raw = generateRawToken();
		String hash = sha256Hex(raw);

		refreshTokenRepository.save(
			RefreshToken.issue(user, hash, now.plus(ttl))
		);

		return raw;
	}

	@Transactional(readOnly = true)
	public Long validate(String raw) {
		String hash = sha256Hex(raw);

		RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
			.orElseThrow(() -> new IllegalStateException("Refresh token not found"));

		Instant now = Instant.now();
		if (token.isRevoked())
			throw new IllegalStateException("Refresh token revoked");
		if (token.isExpired(now))
			throw new IllegalStateException("Refresh token expired");

		return token.getUser().getId();
	}

	@Transactional
	public void revoke(String raw) {
		String hash = sha256Hex(raw);

		RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
			.orElseThrow(() -> new IllegalStateException("Refresh token not found"));

		token.revoke(Instant.now());
	}

	private String generateRawToken() {
		byte[] bytes = new byte[48];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String sha256Hex(String raw) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
