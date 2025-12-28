package com.home.infrastructure.batch.trade;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.home.domain.trade.Trade;
import com.home.domain.trade.TradeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TradeBulkSaver {

	private final TradeRepository tradeRepository;

	@PersistenceContext
	private EntityManager em;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int saveBatch(List<Trade> trades) {
		tradeRepository.saveAll(trades);
		tradeRepository.flush();
		em.clear();
		return trades.size();
	}
}
