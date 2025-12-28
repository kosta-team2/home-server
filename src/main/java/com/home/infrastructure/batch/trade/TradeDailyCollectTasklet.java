package com.home.infrastructure.batch.trade;

import java.time.LocalDate;

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
		LocalDate targetDate = LocalDate.now();

		long inserted = service.collect(targetDate);

		chunkContext.getStepContext()
			.getStepExecution()
			.getJobExecution()
			.getExecutionContext()
			.putLong("todayInsertedCount", inserted);

		return RepeatStatus.FINISHED;
	}
}
