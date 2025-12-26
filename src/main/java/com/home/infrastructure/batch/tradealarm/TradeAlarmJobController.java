package com.home.infrastructure.batch.tradealarm;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// test용임, 실제 운영에서는 스케줄러 쓸거임.
@RestController
public class TradeAlarmJobController {

	private final JobLauncher jobLauncher;
	private final Job tradeAlarmJob;

	public TradeAlarmJobController(JobLauncher jobLauncher, Job tradeAlarmJob) {
		this.jobLauncher = jobLauncher;
		this.tradeAlarmJob = tradeAlarmJob;
	}

	@PostMapping("/admin/batch/trade-alarm/run")
	public String run(@RequestParam(required = false) String runDate) throws Exception {
		if (runDate == null) runDate = LocalDate.now().toString();

		JobParameters params = new JobParametersBuilder()
			.addString("runDate", runDate)
			.addLong("requestTime", System.currentTimeMillis())
			.toJobParameters();

		jobLauncher.run(tradeAlarmJob, params);
		return "tradeAlarmJob launched. runDate=" + runDate;
	}
}
