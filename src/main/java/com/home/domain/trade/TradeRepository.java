package com.home.domain.trade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradeRepository extends JpaRepository<Trade, Long> {

	List<Trade> findByComplex_Id(Long complexId);

	List<Trade> findByComplex_IdIn(List<Long> complexIds);

	@Query("""
SELECT t
FROM Trade t
WHERE t.complex.id IN :complexIds
AND
t.exclArea = :exclArea
AND
t.dealDate BETWEEN :startDate AND :endDate
""")
	List<Trade> findFilteredTradeByComplex_IdIn(List<Long> complexIds, LocalDate startDate, LocalDate endDate, Double exclArea);

}
