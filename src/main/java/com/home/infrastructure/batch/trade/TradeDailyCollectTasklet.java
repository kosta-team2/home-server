package com.home.infrastructure.batch.trade;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@Slf4j
public class TradeDailyCollectTasklet implements Tasklet {

	private final TradeDailyCollectService service;

	public TradeDailyCollectTasklet(TradeDailyCollectService service) {
		this.service = service;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

		String runDateParam = (String)chunkContext.getStepContext()
			.getJobParameters()
			.get("runDate");

		String ymParam = runDateParam.replace("-", "").substring(0, 6);

		YearMonth ym = YearMonth.parse(
			ymParam,
			DateTimeFormatter.ofPattern("yyyyMM")
		);

		LocalDate targetDate = ym.atDay(1);

		TradeDailyCollectService.CollectResult r = service.collect(targetDate);

		var jobExecution = chunkContext.getStepContext()
			.getStepExecution()
			.getJobExecution();

		var jobCtx = jobExecution.getExecutionContext();

		jobCtx.putLong("trade.read",
			jobCtx.getLong("trade.read", 0L) + r.read()
		);

		jobCtx.putLong("trade.write",
			jobCtx.getLong("trade.write", 0L) + r.inserted()
		);

		jobCtx.putLong("trade.skip",
			jobCtx.getLong("trade.skip", 0L) + r.skipped()
		);

		log.info(
			"[BATCH][TRADE_COLLECT] runDate={} → targetYm={}, read={}, inserted={}, skipped={}",
			runDateParam, ym, r.read(), r.inserted(), r.skipped()
		);

		return RepeatStatus.FINISHED;
	}
}
