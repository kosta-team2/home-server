package com.home.infrastructure.batch.tradealarm;

import java.time.LocalDate;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.infrastructure.batch.tradealarm.dto.MailTargetRow;
import com.home.infrastructure.batch.tradealarm.exception.NonRetryableMailException;
import com.home.infrastructure.batch.tradealarm.exception.RetryableMailException;
import com.home.infrastructure.batch.tradealarm.listener.MailSkipListener;
import com.home.infrastructure.batch.tradealarm.processor.MailSendProcessor;
import com.home.infrastructure.batch.tradealarm.reader.MailTargetPagingReaderFactory;
import com.home.infrastructure.batch.tradealarm.writer.MailStatusSuccessWriter;

@Configuration
public class TradeAlarmBatchConfig {

	@Bean
	public Job tradeAlarmJob(JobRepository jobRepository,
		Step buildMailTargetsStep,
		Step sendTradeAlarmMailsStep) {
		return new JobBuilder("tradeAlarmJob", jobRepository)
			.start(buildMailTargetsStep) // 메일 발송 대상 선정해서 table에 insert( tasklet 기반 한 방에 수행)
			.next(sendTradeAlarmMailsStep) // 1개 단위 chunk로 메일 발송 대상 table에서 한 명씩 메일 전송
			.build();
	}

	@Bean
	public Step buildMailTargetsStep(JobRepository jobRepository,
		PlatformTransactionManager tx,
		@Qualifier("oltpJdbc") NamedParameterJdbcTemplate namedJdbc) {
		return new StepBuilder("buildMailTargetsStep", jobRepository)
			.tasklet(new BuildMailTargetsTasklet(namedJdbc), tx)
			.build();
	}

	@Bean
	public Step sendTradeAlarmMailsStep(JobRepository jobRepository,
		PlatformTransactionManager tx,
		JdbcPagingItemReader<MailTargetRow> mailTargetReader,
		MailSendProcessor mailSendProcessor,
		MailStatusSuccessWriter mailStatusSuccessWriter,
		MailSkipListener mailSkipListener) {

		return new StepBuilder("sendTradeAlarmMailsStep", jobRepository)
			.<MailTargetRow, MailTargetRow>chunk(1, tx)
			.reader(mailTargetReader)
			.processor(mailSendProcessor)
			.writer(mailStatusSuccessWriter)
			.faultTolerant()
			.retry(RetryableMailException.class)
			.retryLimit(3)
			.skip(NonRetryableMailException.class)
			.skip(RetryableMailException.class)
			.skipLimit(10_000)
			.listener(mailSkipListener)
			.build();
	}

	@Bean
	@StepScope
	public JdbcPagingItemReader<MailTargetRow> mailTargetReader(
		DataSource dataSource,
		@Value("#{jobParameters['runDate']}") String runDateStr
	) {
		LocalDate runDate = (runDateStr == null) ? LocalDate.now() : LocalDate.parse(runDateStr);
		return MailTargetPagingReaderFactory.create(dataSource, runDate);
	}

	@Bean
	@StepScope
	public MailSendProcessor mailSendProcessor(
		JdbcTemplate jdbcTemplate,
		JavaMailSender mailSender,
		@Value("${spring.mail.username}") String fromAddress,
		@Value("${app.mail.from-name:우리서비스}") String fromName
	) {
		return new MailSendProcessor(jdbcTemplate, mailSender, fromAddress, fromName);
	}

	@Bean
	public MailStatusSuccessWriter mailStatusSuccessWriter(JdbcTemplate jdbcTemplate) {
		return new MailStatusSuccessWriter(jdbcTemplate);
	}

	@Bean
	public MailSkipListener mailSkipListener(JdbcTemplate jdbcTemplate) {
		return new MailSkipListener(jdbcTemplate);
	}
}
