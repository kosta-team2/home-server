package com.home.infrastructure.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.home.domain.user.User;
import com.home.domain.user.UserRole;
import com.home.infrastructure.security.oauth.UserDetailPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final JwtProperties props;
	private final SecretKey key;

	public JwtTokenProvider(JwtProperties props) {
		this.props = props;
		this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Instant exp = now.plus(props.getAccessMinutes(), ChronoUnit.MINUTES);

		Map<String, Object> claims = Map.of(
			"userId", String.valueOf(user.getId()),
			"role", user.getRole().name(),
			"name", user.getUserName(),
			"email", user.getUserEmail()
		);

		return Jwts.builder()
			.issuer(props.getIssuer())
			.claims(claims)
			.issuedAt(Date.from(now))
			.expiration(Date.from(exp))
			.signWith(key)
			.compact();
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);

		Long userId = Long.parseLong((String)claims.get("userId"));
		UserRole role = UserRole.valueOf((String)claims.get("role"));

		User user = User.builder()
			.id(userId)
			.role(role)
			.userName((String)claims.get("name"))
			.userEmail((String)claims.get("email"))
			.build();

		var authorities = Set.of(new SimpleGrantedAuthority(role.getValue()));
		var principal = new UserDetailPrincipal(user, authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean validate(String token) {
		parseClaims(token);
		return true;
	}
}
