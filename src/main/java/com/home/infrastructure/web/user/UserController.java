package com.home.infrastructure.web.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.home.domain.user.User;
import com.home.domain.user.UserRepository;
import com.home.infrastructure.security.oauth.UserDetailPrincipal;
import com.home.infrastructure.web.user.dto.MeMiniResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;

	private Long userId(UserDetailPrincipal principal) {
		if (principal == null) {
			throw new IllegalArgumentException("unauthorized");
		}
		return principal.getUserId();
	}

	@GetMapping("/me")
	public ResponseEntity<MeMiniResponse> me(
		@AuthenticationPrincipal UserDetailPrincipal principal
	) {
		Long uid = userId(principal);

		User user = userRepository.findById(uid)
			.orElseThrow(() -> new IllegalArgumentException("user not found"));

		return ResponseEntity.ok(MeMiniResponse.from(user));
	}
}
