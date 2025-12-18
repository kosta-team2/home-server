package com.home.infrastructure.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.infrastructure.batch.trade.TradeInitStepListener;
import com.home.infrastructure.batch.trade.TradeInitTasklet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class TradeInitJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	public TradeInitJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Bean
	public Job tradeInitJob(Step tradeStep) {
		return new JobBuilder("tradeInitJob", jobRepository)
			.start(tradeStep)
			.build();
	}

	@Bean
	public Step tradeStep(
		TradeInitTasklet tasklet,
		TradeInitStepListener listener
	) {
		return new StepBuilder("tradeStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(listener)
			.build();
	}

}
