package com.home.infrastructure.batch.trade;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TradeInitStepListener implements StepExecutionListener {

	private static final String EC_READ = "trade.read";
	private static final String EC_SAVED = "trade.saved";

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		var ec = stepExecution.getExecutionContext();

		long read = ec.containsKey(EC_READ) ? ec.getLong(EC_READ) : 0L;
		long saved = ec.containsKey(EC_SAVED) ? ec.getLong(EC_SAVED) : 0L;

		log.info("[BATCH][tradeInit][SUMMARY] 읽어드린 값={}, 저장한 값={}, 저장 실패한 값={}",
			read, saved, (read - saved));

		return stepExecution.getExitStatus();
	}
}
