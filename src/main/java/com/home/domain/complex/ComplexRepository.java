package com.home.domain.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.home.infrastructure.web.search.dto.ComplexSearchResponse;

public interface ComplexRepository extends JpaRepository<Complex, Long> {
	Optional<Complex> findByAptSeq(String aptSeq);

	Optional<Complex> findByTradeName(String tradeName);

	List<Complex> findAllByParcel_Id(Long parcelId);

	@Query("""
			SELECT new com.home.infrastructure.web.search.dto.ComplexSearchResponse(
				c.id,
				COALESCE(NULLIF(c.tradeName, ''), c.name),
				p.id,
				p.latitude,
				p.longitude,
				p.address
			)
			FROM Complex c
			LEFT JOIN c.parcel p
			WHERE
				LOWER(c.name)      LIKE LOWER(CONCAT('%', :q, '%'))
				OR LOWER(c.tradeName) LIKE LOWER(CONCAT('%', :q, '%'))
			ORDER BY
				CASE
					WHEN LOWER(c.name) = LOWER(:q) OR LOWER(c.tradeName) = LOWER(:q) THEN 0
					ELSE 1
				END,
				LEAST(
					NULLIF(LOCATE(LOWER(:q), LOWER(c.tradeName)), 0),
					NULLIF(LOCATE(LOWER(:q), LOWER(c.name)), 0),
					999999
				),
				c.name ASC
		""")
	List<ComplexSearchResponse> searchComplexesWithParcel(@Param("q") String q, Pageable pageable);
}
