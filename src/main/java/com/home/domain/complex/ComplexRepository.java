package com.home.domain.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComplexRepository extends JpaRepository<Complex, Long> {
	Optional<Complex> findByAptSeq(String aptSeq);

	Optional<Complex> findByTradeName(String tradeName);

	@Query("SELECT c FROM Complex c " +
		"WHERE c.latitude BETWEEN :swLat AND :neLat " +
		"AND c.longitude BETWEEN :swLng AND :neLng")
	List<Complex> findAllByBoundary(
		@Param("swLat") Double swLat,
		@Param("swLng") Double swLng,
		@Param("neLat") Double neLat,
		@Param("neLng") Double neLng
	);

	List<Complex> findAllByParcel_Id(Long parcelId);

}
