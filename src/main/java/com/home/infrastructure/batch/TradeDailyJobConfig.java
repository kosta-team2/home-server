package com.home.infrastructure.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.infrastructure.batch.common.BatchSummaryListener;
import com.home.infrastructure.batch.trade.TradeDailyCollectTasklet;
import com.home.infrastructure.batch.trade.TradeTopPriceTasklet;
import com.home.infrastructure.batch.trade.TradeTopVolumeTasklet;
import com.home.infrastructure.batch.trade.TradeTrendTasklet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("batch")
public class TradeDailyJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager tx;

	public TradeDailyJobConfig(
		JobRepository jobRepository,
		PlatformTransactionManager tx
	) {
		this.jobRepository = jobRepository;
		this.tx = tx;
	}

	@Bean
	public Job tradeDailyJob(
		Step tradeCollectStep,
		Step tradeTrendStep,
		Step tradeTopVolumeStep,
		Step tradeTopPriceStep,
		Step sendTradeAlarmMailsStep,
		BatchSummaryListener batchSummaryListener
	) {
		return new JobBuilder("tradeDailyJob", jobRepository)
			.listener(batchSummaryListener)
			.start(tradeCollectStep)
			.next(tradeTrendStep)
			.next(tradeTopVolumeStep)
			.next(tradeTopPriceStep)
			.next(sendTradeAlarmMailsStep)
			.build();
	}

	@Bean
	public Step tradeCollectStep(
		TradeDailyCollectTasklet tasklet
	) {
		return new StepBuilder("tradeCollectStep", jobRepository)
			.tasklet(tasklet, tx)
			.build();
	}

	@Bean
	public Step tradeTrendStep(
		TradeTrendTasklet tasklet
	) {
		return new StepBuilder("tradeTrendStep", jobRepository)
			.tasklet(tasklet, tx)
			.build();
	}

	@Bean
	public Step tradeTopVolumeStep(
		TradeTopVolumeTasklet tasklet
	) {
		return new StepBuilder("tradeTopVolumeStep", jobRepository)
			.tasklet(tasklet, tx)
			.build();
	}

	@Bean
	public Step tradeTopPriceStep(
		TradeTopPriceTasklet tasklet
	) {
		return new StepBuilder("tradeTopPriceStep", jobRepository)
			.tasklet(tasklet, tx)
			.build();
	}
}
