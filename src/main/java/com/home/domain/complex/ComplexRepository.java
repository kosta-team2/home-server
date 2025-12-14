package com.home.domain.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ComplexRepository extends JpaRepository<Complex, Long> {
	Optional<Complex> findByAptSeq(String aptSeq);

	Optional<Complex> findByTradeName(String tradeName);

	List<Complex> findAllByParcel_Id(Long parcelId);

	@Query("SELECT c.id FROM Complex c WHERE c.parcel.id = :parcelId")
	List<Long> findAllIdsByParcel_Id(Long parcelId);
}
