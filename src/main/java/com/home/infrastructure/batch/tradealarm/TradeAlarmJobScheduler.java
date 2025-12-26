package com.home.infrastructure.batch.tradealarm;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradeAlarmJobScheduler {

	private final JobLauncher jobLauncher;
	private final Job tradeAlarmJob;

	public TradeAlarmJobScheduler(JobLauncher jobLauncher, Job tradeAlarmJob) {
		this.jobLauncher = jobLauncher;
		this.tradeAlarmJob = tradeAlarmJob;
	}

	@Scheduled(cron = "0 00 8 * * *") // 매일 아침 8시
	public void runDaily() throws Exception {
		String runDate = LocalDate.now().toString();

		JobParameters params = new JobParametersBuilder()
			.addString("runDate", runDate)
			.addLong("requestTime", System.currentTimeMillis())
			.toJobParameters();

		jobLauncher.run(tradeAlarmJob, params);
	}
}
