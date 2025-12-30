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

	public TradeDailyCollectTasklet(
		TradeDailyCollectService service
	) {
		this.service = service;
	}

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		String runDateParam = (String) chunkContext.getStepContext()
			.getJobParameters()
			.get("runDate");

		YearMonth ym = YearMonth.parse(
			runDateParam,
			DateTimeFormatter.ofPattern("yyyyMM")
		);

		LocalDate targetDate = ym.atDay(1);

		long insertedCount = service.collect(targetDate);

		chunkContext.getStepContext()
			.getStepExecution()
			.getJobExecution()
			.getExecutionContext()
			.putLong("dailyInsertedCount", insertedCount);

		log.info(
			"[BATCH][TRADE_COLLECT] targetYm={}, inserted={}",
			ym, insertedCount
		);

		return RepeatStatus.FINISHED;
	}
}
