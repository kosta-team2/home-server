package com.home.application.favorite;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.home.annotations.MockTest;
import com.home.domain.favorite.FavoriteParcel;
import com.home.domain.favorite.FavoriteParcelRepository;
import com.home.domain.parcel.Parcel;
import com.home.domain.parcel.ParcelRepository;
import com.home.domain.user.User;
import com.home.domain.user.UserRepository;
import com.home.global.exception.ErrorCode;
import com.home.global.exception.common.NotFoundException;
import com.home.global.exception.common.UnauthorizedException;
import com.home.infrastructure.web.favorite.dto.FavoriteResponse;

@MockTest
class FavoriteUseCaseTest {

	@Mock FavoriteParcelRepository favoriteParcelRepository;
	@Mock UserRepository userRepository;
	@Mock ParcelRepository parcelRepository;

	@InjectMocks FavoriteUseCase favoriteUseCase;

	@Test
	@DisplayName("addFavorite: 이미 즐겨찾기가 있으면 그대로 반환하고 save/user/parcel 조회를 하지 않는다")
	void addFavorite_returnsExisting() {
		Long userId = 1L;
		Long parcelId = 10L;

		// FavoriteResponse.from()이 호출하는 getter만 stub
		FavoriteParcel existing = mock(FavoriteParcel.class);
		given(existing.getId()).willReturn(99L);
		given(existing.getComplexName()).willReturn("기존단지");
		given(existing.isAlarmEnabled()).willReturn(true);

		Parcel parcel = mock(Parcel.class);
		given(parcel.getId()).willReturn(parcelId);
		given(parcel.getAddress()).willReturn("addr");
		given(parcel.getLatitude()).willReturn(37.1);
		given(parcel.getLongitude()).willReturn(127.1);
		given(existing.getParcel()).willReturn(parcel);

		given(favoriteParcelRepository.findByUser_IdAndParcel_Id(userId, parcelId))
			.willReturn(Optional.of(existing));

		FavoriteResponse res = favoriteUseCase.addFavorite(userId, parcelId, "무시됨");

		assertThat(res.id()).isEqualTo(99L);
		assertThat(res.parcelId()).isEqualTo(parcelId);
		assertThat(res.complexName()).isEqualTo("기존단지");
		assertThat(res.address()).isEqualTo("addr");
		assertThat(res.lat()).isEqualTo(37.1);
		assertThat(res.lng()).isEqualTo(127.1);
		assertThat(res.alarmEnabled()).isTrue();

		verify(favoriteParcelRepository, never()).save(any());
		verify(userRepository, never()).findById(anyLong());
		verify(parcelRepository, never()).findById(anyLong());
	}

	@Test
	@DisplayName("addFavorite: 신규 등록 시 user가 없으면 UnauthorizedException")
	void addFavorite_userNotFound() {
		Long userId = 1L;
		Long parcelId = 10L;

		given(favoriteParcelRepository.findByUser_IdAndParcel_Id(userId, parcelId))
			.willReturn(Optional.empty());
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> favoriteUseCase.addFavorite(userId, parcelId, "단지"))
			.isInstanceOf(UnauthorizedException.class);

		verify(parcelRepository, never()).findById(anyLong());
		verify(favoriteParcelRepository, never()).save(any());
	}

	@Test
	@DisplayName("addFavorite: 신규 등록 시 parcel이 없으면 NotFoundException(DATA_NOT_FOUND)")
	void addFavorite_parcelNotFound() {
		Long userId = 1L;
		Long parcelId = 10L;

		given(favoriteParcelRepository.findByUser_IdAndParcel_Id(userId, parcelId))
			.willReturn(Optional.empty());

		User user = mock(User.class);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		given(parcelRepository.findById(parcelId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> favoriteUseCase.addFavorite(userId, parcelId, "단지"))
			.isInstanceOf(NotFoundException.class)
			.satisfies(ex -> {
				NotFoundException e = (NotFoundException) ex;
				assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND);
			});

		verify(favoriteParcelRepository, never()).save(any());
	}

	@Test
	@DisplayName("addFavorite: complexName이 null/blank면 '관심 단지'로 저장한다")
	void addFavorite_blankName_defaultsToKorean() {
		Long userId = 1L;
		Long parcelId = 10L;

		given(favoriteParcelRepository.findByUser_IdAndParcel_Id(userId, parcelId))
			.willReturn(Optional.empty());

		User user = mock(User.class);
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		Parcel parcel = mock(Parcel.class);
		given(parcel.getId()).willReturn(parcelId);
		given(parcel.getAddress()).willReturn("addr");
		given(parcel.getLatitude()).willReturn(37.1);
		given(parcel.getLongitude()).willReturn(127.1);
		given(parcelRepository.findById(parcelId)).willReturn(Optional.of(parcel));

		// save(arg) -> arg 그대로 반환
		given(favoriteParcelRepository.save(any(FavoriteParcel.class)))
			.willAnswer(inv -> inv.getArgument(0));

		FavoriteResponse res = favoriteUseCase.addFavorite(userId, parcelId, "   ");

		assertThat(res.parcelId()).isEqualTo(parcelId);
		assertThat(res.complexName()).isEqualTo("관심 단지");
		assertThat(res.address()).isEqualTo("addr");
	}

	@Test
	@DisplayName("list: userId로 즐겨찾기 목록을 조회하고 FavoriteResponse로 매핑한다")
	void list_mapsToResponse() {
		Long userId = 1L;

		// f1
		FavoriteParcel f1 = mock(FavoriteParcel.class);
		given(f1.getId()).willReturn(100L);
		given(f1.getComplexName()).willReturn("단지1");
		given(f1.isAlarmEnabled()).willReturn(true);

		Parcel p1 = mock(Parcel.class);
		given(p1.getId()).willReturn(10L);
		given(p1.getAddress()).willReturn("addr1");
		given(p1.getLatitude()).willReturn(37.1);
		given(p1.getLongitude()).willReturn(127.1);
		given(f1.getParcel()).willReturn(p1);

		// f2
		FavoriteParcel f2 = mock(FavoriteParcel.class);
		given(f2.getId()).willReturn(101L);
		given(f2.getComplexName()).willReturn("단지2");
		given(f2.isAlarmEnabled()).willReturn(false);

		Parcel p2 = mock(Parcel.class);
		given(p2.getId()).willReturn(11L);
		given(p2.getAddress()).willReturn("addr2");
		given(p2.getLatitude()).willReturn(37.2);
		given(p2.getLongitude()).willReturn(127.2);
		given(f2.getParcel()).willReturn(p2);

		given(favoriteParcelRepository.findAllByUser_IdOrderByCreatedAtDesc(userId))
			.willReturn(List.of(f1, f2));

		List<FavoriteResponse> res = favoriteUseCase.list(userId);

		assertThat(res).hasSize(2);
		assertThat(res.get(0).id()).isEqualTo(100L);
		assertThat(res.get(0).complexName()).isEqualTo("단지1");
		assertThat(res.get(1).id()).isEqualTo(101L);
		assertThat(res.get(1).alarmEnabled()).isFalse();
	}

	@Test
	@DisplayName("updateAlarm: 본인 즐겨찾기면 alarmEnabled 변경 후 응답 반환")
	void updateAlarm_success() {
		Long userId = 1L;
		Long favoriteId = 99L;

		FavoriteParcel f = mock(FavoriteParcel.class);

		User user = mock(User.class);
		given(user.getId()).willReturn(userId);
		given(f.getUser()).willReturn(user);

		given(favoriteParcelRepository.findById(favoriteId)).willReturn(Optional.of(f));

		// 응답 생성에 필요한 최소 stub
		given(f.getId()).willReturn(favoriteId);
		given(f.getComplexName()).willReturn("단지");
		given(f.isAlarmEnabled()).willReturn(true); // enabled 값이 반영됐다고 가정(엔티티 setter는 void)

		Parcel parcel = mock(Parcel.class);
		given(parcel.getId()).willReturn(10L);
		given(parcel.getAddress()).willReturn("addr");
		given(parcel.getLatitude()).willReturn(37.1);
		given(parcel.getLongitude()).willReturn(127.1);
		given(f.getParcel()).willReturn(parcel);

		FavoriteResponse res = favoriteUseCase.updateAlarm(userId, favoriteId, true);

		verify(f).setAlarmEnabled(true);
		assertThat(res.id()).isEqualTo(favoriteId);
	}

	@Test
	@DisplayName("updateAlarm: 다른 사람 즐겨찾기면 UnauthorizedException(forbidden)")
	void updateAlarm_forbidden() {
		Long userId = 1L;
		Long favoriteId = 99L;

		FavoriteParcel f = mock(FavoriteParcel.class);

		User other = mock(User.class);
		given(other.getId()).willReturn(2L);
		given(f.getUser()).willReturn(other);

		given(favoriteParcelRepository.findById(favoriteId)).willReturn(Optional.of(f));

		assertThatThrownBy(() -> favoriteUseCase.updateAlarm(userId, favoriteId, true))
			.isInstanceOf(UnauthorizedException.class);

		verify(f, never()).setAlarmEnabled(anyBoolean());
	}

	@Test
	@DisplayName("remove: 본인 즐겨찾기면 delete를 호출한다")
	void remove_success() {
		Long userId = 1L;
		Long favoriteId = 99L;

		FavoriteParcel f = mock(FavoriteParcel.class);

		User user = mock(User.class);
		given(user.getId()).willReturn(userId);
		given(f.getUser()).willReturn(user);

		given(favoriteParcelRepository.findById(favoriteId)).willReturn(Optional.of(f));

		favoriteUseCase.remove(userId, favoriteId);

		verify(favoriteParcelRepository).delete(f);
	}

	@Test
	@DisplayName("remove: 다른 사람 즐겨찾기면 UnauthorizedException(forbidden) + delete 호출 안함")
	void remove_forbidden() {
		Long userId = 1L;
		Long favoriteId = 99L;

		FavoriteParcel f = mock(FavoriteParcel.class);

		User other = mock(User.class);
		given(other.getId()).willReturn(2L);
		given(f.getUser()).willReturn(other);

		given(favoriteParcelRepository.findById(favoriteId)).willReturn(Optional.of(f));

		assertThatThrownBy(() -> favoriteUseCase.remove(userId, favoriteId))
			.isInstanceOf(UnauthorizedException.class);

		verify(favoriteParcelRepository, never()).delete(any());
	}

	@Test
	@DisplayName("exists: repository 결과를 그대로 반환한다")
	void exists_returnsRepoValue() {
		Long userId = 1L;
		Long parcelId = 10L;

		given(favoriteParcelRepository.existsByUser_IdAndParcel_Id(userId, parcelId))
			.willReturn(true);

		boolean res = favoriteUseCase.exists(userId, parcelId);

		assertThat(res).isTrue();
		verify(favoriteParcelRepository).existsByUser_IdAndParcel_Id(userId, parcelId);
	}
}
