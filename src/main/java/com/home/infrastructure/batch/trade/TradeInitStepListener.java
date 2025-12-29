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

		String dealYmd = ec.containsKey("dealYmd") ? ec.getString("dealYmd") : "UNKNOWN";
		long read = ec.containsKey(EC_READ) ? ec.getLong(EC_READ) : 0L;
		long saved = ec.containsKey(EC_SAVED) ? ec.getLong(EC_SAVED) : 0L;

		log.info("[BATCH][tradeInit][PARTITION] dealYmd={} read={} saved={} diff={}",
			dealYmd, read, saved, (read - saved));

		return stepExecution.getExitStatus();
	}
}
