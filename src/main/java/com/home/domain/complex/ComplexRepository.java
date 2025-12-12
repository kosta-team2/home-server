package com.home.domain.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplexRepository extends JpaRepository<Complex, Long> {
	Optional<Complex> findByAptSeq(String aptSeq);

	List<Complex> findByTradeName(String tradeName);

}
