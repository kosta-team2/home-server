package com.home.infrastructure.batch;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.home.domain.complex.Complex;
import com.home.domain.parcel.Parcel;
import com.home.infrastructure.batch.complex.dto.ComplexRowRequest;
import com.home.infrastructure.batch.parcel.dto.ParcelRowRequest;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ComplexInitJobConfig {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	public ComplexInitJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Bean
	public Job complexInitJob(Step parcelStep, Step complexStep) {
		return new JobBuilder("complexInitJob", jobRepository)
			.start(parcelStep)
			.next(complexStep)
			.build();
	}

	@Bean
	public Step parcelStep(
		FlatFileItemReader<ParcelRowRequest> parcelItemReader,
		ItemProcessor<ParcelRowRequest, Parcel> parcelItemProcessor,
		ItemWriter<Parcel> parcelItemWriter
	) {
		return new StepBuilder("parcelStep", jobRepository)
			.<ParcelRowRequest, Parcel>chunk(1, transactionManager)
			.reader(parcelItemReader)
			.processor(parcelItemProcessor)
			.writer(parcelItemWriter)
			.build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<ParcelRowRequest> parcelItemReader(
		@Value("#{jobParameters['inputFile']}") String inputFile
	) {
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(",");
		tokenizer.setQuoteCharacter('"');
		tokenizer.setStrict(false);
		tokenizer.setNames(
			"complexPk", "pnu", "address", "tradeName", "conName", "roadName",
			"gbCd", "dongCnt", "unitCnt", "useApt_dt"
		);

		return new FlatFileItemReaderBuilder<ParcelRowRequest>()
			.name("parcelItemReader")
			.resource(new FileSystemResource(inputFile))
			.encoding(StandardCharsets.UTF_8.name())
			.linesToSkip(1)
			.lineTokenizer(tokenizer)
			.fieldSetMapper(fs -> new ParcelRowRequest(
				fs.readString("pnu"),
				fs.readString("address"),
				fs.readString("gbCd")
			))
			.build();
	}

	@Bean
	public Step complexStep(
		SynchronizedItemStreamReader<ComplexRowRequest> complexItemReader,
		ItemProcessor<ComplexRowRequest, Complex> complexItemProcessor,
		ItemWriter<Complex> complexItemWriter,
		TaskExecutor complexTaskExecutor
	) {
		return new StepBuilder("complexStep", jobRepository)
			.<ComplexRowRequest, Complex>chunk(100, transactionManager)
			.reader(complexItemReader)
			.processor(complexItemProcessor)
			.writer(complexItemWriter)
			.taskExecutor(complexTaskExecutor)
			.build();
	}

	@Bean
	public JpaItemWriter<Parcel> parcelItemWriter(EntityManagerFactory emf) {
		return new JpaItemWriterBuilder<Parcel>()
			.entityManagerFactory(emf)
			.usePersist(true)
			.build();
	}

	@Bean
	public TaskExecutor complexTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(10);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(10);
		executor.setThreadNamePrefix("complex-batch-");
		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(30);
		executor.setQueueCapacity(0);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	@Bean
	@StepScope
	public FlatFileItemReader<ComplexRowRequest> complexItemReaderDelegate(
		@Value("#{jobParameters['inputFile']}") String inputFile
	) {
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(",");
		tokenizer.setQuoteCharacter('"');
		tokenizer.setStrict(false);
		tokenizer.setNames("complexPk", "pnu", "address", "tradeName", "conName", "roadName", "gbCd", "dongCnt",
			"unitCnt", "useApt_dt");

		return new FlatFileItemReaderBuilder<ComplexRowRequest>()
			.name("complexItemReader")
			.resource(new FileSystemResource(inputFile))
			.encoding(StandardCharsets.UTF_8.name())
			.lineTokenizer(tokenizer)
			.fieldSetMapper(fs -> new ComplexRowRequest(
				fs.readString("complexPk"),
				fs.readString("pnu"),
				fs.readString("address"),
				fs.readString("tradeName"),
				fs.readString("conName"),
				fs.readString("roadName"),
				fs.readString("gbCd"),
				fs.readInt("dongCnt"),
				fs.readInt("unitCnt"),
				fs.readString("useApt_dt")
			))
			.build();
	}

	@Bean
	@StepScope
	public SynchronizedItemStreamReader<ComplexRowRequest> complexItemReader(
		FlatFileItemReader<ComplexRowRequest> complexItemReaderDelegate
	) {
		return new SynchronizedItemStreamReaderBuilder<ComplexRowRequest>()
			.delegate(complexItemReaderDelegate)
			.build();
	}

	@Bean
	public JpaItemWriter<Complex> complexItemWriter(EntityManagerFactory emf) {
		return new JpaItemWriterBuilder<Complex>()
			.entityManagerFactory(emf)
			.usePersist(true)
			.build();
	}

}
