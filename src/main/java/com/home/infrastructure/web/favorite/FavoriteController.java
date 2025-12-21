package com.home.infrastructure.web.favorite;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.home.application.favorite.FavoriteUseCase;
import com.home.infrastructure.security.oauth.UserDetailPrincipal;
import com.home.infrastructure.web.favorite.dto.FavoriteAlarmRequest;
import com.home.infrastructure.web.favorite.dto.FavoriteCreateRequest;
import com.home.infrastructure.web.favorite.dto.FavoriteResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

	private final FavoriteUseCase favoriteUseCase;

	private Long userId(UserDetailPrincipal principal) {
		if (principal == null) {
			throw new IllegalArgumentException("unauthorized");
		}
		return principal.getUserId();
	}

	@PostMapping
	public ResponseEntity<FavoriteResponse> add(
		@AuthenticationPrincipal UserDetailPrincipal principal,
		@RequestBody FavoriteCreateRequest req
	) {
		return ResponseEntity.ok(
			favoriteUseCase.addFavorite(userId(principal), req.parcelId(), req.complexName())
		);
	}

	@GetMapping
	public ResponseEntity<List<FavoriteResponse>> list(
		@AuthenticationPrincipal UserDetailPrincipal principal
	) {
		return ResponseEntity.ok(favoriteUseCase.list(userId(principal)));
	}

	@PatchMapping("/{favoriteId}/alarm")
	public ResponseEntity<FavoriteResponse> setAlarm(
		@AuthenticationPrincipal UserDetailPrincipal principal,
		@PathVariable Long favoriteId,
		@RequestBody FavoriteAlarmRequest req
	) {
		return ResponseEntity.ok(
			favoriteUseCase.updateAlarm(userId(principal), favoriteId, req.enabled())
		);
	}

	@DeleteMapping("/{favoriteId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal UserDetailPrincipal principal,
		@PathVariable Long favoriteId
	) {
		favoriteUseCase.remove(userId(principal), favoriteId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/exists")
	public ResponseEntity<Boolean> exists(
		@AuthenticationPrincipal UserDetailPrincipal principal,
		@RequestParam Long parcelId
	) {
		return ResponseEntity.ok(favoriteUseCase.exists(userId(principal), parcelId));
	}
}
