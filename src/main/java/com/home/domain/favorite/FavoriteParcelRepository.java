package com.home.domain.favorite;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteParcelRepository extends JpaRepository<FavoriteParcel, Long> {

	List<FavoriteParcel> findAllByUser_IdOrderByCreatedAtDesc(Long userId);

	Optional<FavoriteParcel> findByUser_IdAndParcel_Id(Long userId, Long parcelId);

	boolean existsByUser_IdAndParcel_Id(Long userId, Long parcelId);

	void deleteByUser_IdAndParcel_Id(Long userId, Long parcelId);
}
