package com.home.application.favorite;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.favorite.FavoriteParcel;
import com.home.domain.favorite.FavoriteParcelRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.user.User;
import com.home.domain.user.UserRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.ForbiddenException;
import com.home.global.exception.common.NotFoundException;
import com.home.infrastructure.web.favorite.dto.FavoriteResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteUseCase {

	private final FavoriteParcelRepository favoriteParcelRepository;
	private final UserRepository userRepository;
	private final ParcelRepository parcelRepository;

	@Transactional
	public FavoriteResponse addFavorite(Long userId, Long parcelId, String complexName) {
		return favoriteParcelRepository.findByUser_IdAndParcel_Id(userId, parcelId)
			.map(FavoriteResponse::from)
			.orElseGet(() -> {
				User user = userRepository.findById(userId)
					.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "user not found"));

				Parcel parcel = parcelRepository.findById(parcelId)
					.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "parcel not found"));

				String name = (complexName == null || complexName.isBlank()) ? "관심 단지" : complexName.trim();

				FavoriteParcel saved = favoriteParcelRepository.save(FavoriteParcel.create(user, parcel, name));
				return FavoriteResponse.from(saved);
			});
	}

	@Transactional(readOnly = true)
	public List<FavoriteResponse> list(Long userId) {
		return favoriteParcelRepository.findAllByUser_IdOrderByCreatedAtDesc(userId)
			.stream()
			.map(FavoriteResponse::from)
			.toList();
	}

	@Transactional
	public FavoriteResponse updateAlarm(Long userId, Long favoriteId, boolean enabled) {
		FavoriteParcel f = favoriteParcelRepository.findById(favoriteId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "favorite not found"));

		if (!f.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorCode.AUTH_ERROR, "forbidden");
		}

		f.setAlarmEnabled(enabled);
		return FavoriteResponse.from(f);
	}

	@Transactional
	public void remove(Long userId, Long favoriteId) {
		FavoriteParcel f = favoriteParcelRepository.findById(favoriteId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.DATA_NOT_FOUND, "favorite not found"));

		if (!f.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorCode.AUTH_ERROR, "forbidden");
		}

		favoriteParcelRepository.delete(f);
	}

	@Transactional(readOnly = true)
	public boolean exists(Long userId, Long parcelId) {
		return favoriteParcelRepository.existsByUser_IdAndParcel_Id(userId, parcelId);
	}
}
