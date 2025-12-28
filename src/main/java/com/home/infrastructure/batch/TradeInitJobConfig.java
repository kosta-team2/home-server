package com.home.infrastructure.batch;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.infrastructure.batch.trade.TradeInitStepListener;
import com.home.infrastructure.batch.trade.TradeInitTasklet;
import com.home.infrastructure.batch.trade.TradeMonthSggPartitioner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("batch")
@Configuration
public class TradeInitJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final NamedParameterJdbcTemplate olapJdbc;

	public TradeInitJobConfig(
		JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		NamedParameterJdbcTemplate olapJdbc
	) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.olapJdbc = olapJdbc;
	}

	@Bean
	public Job tradeInitJob(Step tradeMasterStep) {
		return new JobBuilder("tradeInitJob", jobRepository)
			.start(tradeMasterStep)
			.build();
	}

	@Bean
	public Step tradeMasterStep(
		Step tradeWorkerStep,
		Partitioner tradePartitioner,
		TaskExecutor tradeInitTaskExecutor
	) {
		return new StepBuilder("tradeMasterStep", jobRepository)
			.partitioner("tradeWorkerStep", tradePartitioner)
			.step(tradeWorkerStep)
			.gridSize(8)
			.taskExecutor(tradeInitTaskExecutor)
			.build();
	}

	@Bean
	public Step tradeWorkerStep(
		TradeInitTasklet tasklet,
		TradeInitStepListener listener
	) {
		return new StepBuilder("tradeWorkerStep", jobRepository)
			.tasklet(tasklet, transactionManager)
			.listener(listener)
			.build();
	}

	@Bean
	public Partitioner tradePartitioner() {
		List<String> sggCodes = olapJdbc.queryForList(
			"select sgg_code from region where level = :level",
			Map.of("level", "SIGUNGU"),
			String.class
		);

		return new TradeMonthSggPartitioner(
			sggCodes,
			YearMonth.of(2010, 1),
			YearMonth.of(2025, 12),
			30
		);
	}

	@Bean
	public TaskExecutor tradeInitTaskExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		exec.setThreadNamePrefix("trade-init-");
		exec.setCorePoolSize(8);
		exec.setMaxPoolSize(8);
		exec.setQueueCapacity(200);
		exec.initialize();
		return exec;
	}
}
