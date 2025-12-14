package com.home.domain.trade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradeRepository extends JpaRepository<Trade, Long> {

	List<Trade> findByComplex_Id(Long complexId);

	@Query("""
SELECT t
FROM Trade t
WHERE t.complex.id = :complexId
AND
t.exclArea = :exclArea
AND
t.dealDate BETWEEN :startDate AND :endDate
""")
	List<Trade> findByComplex_IdWithFilter(Long complexId, LocalDate startDate, LocalDate endDate, Double exclArea);
}
