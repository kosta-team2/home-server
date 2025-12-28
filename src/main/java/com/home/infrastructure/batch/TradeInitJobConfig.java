package com.home.infrastructure.batch;

import java.time.YearMonth;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.infrastructure.batch.trade.TradeInitStepListener;
import com.home.infrastructure.batch.trade.TradeInitTasklet;
import com.home.infrastructure.batch.trade.TradeMonthPartitioner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("batch")
@Configuration
public class TradeInitJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	public TradeInitJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Bean
	public Job tradeInitJob(Step tradeMasterStep) {
		return new JobBuilder("tradeInitJob", jobRepository)
			.start(tradeMasterStep)
			.build();
	}

	@Bean
	public Step tradeMasterStep(Step tradeWorkerStep, Partitioner tradeMonthPartitioner,
		TaskExecutor tradeInitTaskExecutor) {
		int gridSize = 8;

		return new StepBuilder("tradeMasterStep", jobRepository)
			.partitioner("tradeWorkerStep", tradeMonthPartitioner)
			.step(tradeWorkerStep)
			.gridSize(gridSize)
			.taskExecutor(tradeInitTaskExecutor)
			.build();
	}

	@Bean
	public Step tradeWorkerStep(TradeInitTasklet tasklet, TradeInitStepListener listener) {
		return new StepBuilder("tradeWorkerStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(listener)
			.build();
	}

	@Bean
	public Partitioner tradeMonthPartitioner() {
		return new TradeMonthPartitioner(YearMonth.of(2010, 1), YearMonth.of(2025, 12));
	}

	@Bean
	public TaskExecutor tradeInitTaskExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		exec.setThreadNamePrefix("trade-init-");
		exec.setCorePoolSize(8);
		exec.setMaxPoolSize(8);
		exec.setQueueCapacity(0);
		exec.initialize();
		return exec;
	}
}
