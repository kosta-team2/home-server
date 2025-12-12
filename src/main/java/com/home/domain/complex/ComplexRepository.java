package com.home.domain.complex;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplexRepository extends JpaRepository<Complex, Long> {
	Optional<Complex> findByAptSeq(String aptSeq);

	Optional<Complex> findByTradeName(String tradeName);
}
