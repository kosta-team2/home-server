package com.home.infrastructure.batch.common;

import java.time.Duration;
import java.util.Collection;

import org.springframework.batch.core.*;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSummaryListener extends JobExecutionListenerSupport {

	private final SlackNotifier slackNotifier;

	@Override
	public void afterJob(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();

		String runDate = jobExecution.getJobParameters().getString("runDate", "-");

		Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
		String durationStr = format(duration);

		Counters trade = sumStep(jobExecution.getStepExecutions(), "trade");

		int trendUpdated = sumCtxInt(jobExecution.getStepExecutions(), "trend.updated");
		int mailTarget = sumCtxInt(jobExecution.getStepExecutions(), "mail.target");
		int mailSent = sumCtxInt(jobExecution.getStepExecutions(), "mail.sent");
		int mailFailed = sumCtxInt(jobExecution.getStepExecutions(), "mail.failed");

		long partitions = jobExecution.getStepExecutions().stream()
			.filter(se -> se.getStepName().startsWith("tradeWorkerStep"))
			.count();

		int threads = 8;

		boolean success = jobExecution.getStatus() == BatchStatus.COMPLETED;

		String message = buildMessage(
			success, jobName, runDate,
			trade.read, trade.write, trade.filterOrSkip,
			trendUpdated,
			mailTarget, mailSent, mailFailed,
			threads, partitions,
			durationStr,
			jobExecution
		);

		if (success)
			log.info(message);
		else
			log.error(message);

		slackNotifier.send(message);
	}

	private static String buildMessage(
		boolean success,
		String jobName,
		String runDate,
		long tradeRead,
		long tradeWrite,
		long tradeSkip,
		int trendUpdated,
		int mailTarget,
		int mailSent,
		int mailFailed,
		int threads,
		long partitions,
		String duration,
		JobExecution jobExecution
	) {
		String icon = success ? "✅" : "❌";

		StringBuilder sb = new StringBuilder();
		sb.append(icon)
			.append(" [HomeSearch][").append(jobName).append("] 배치 ")
			.append(success ? "완료" : "실패")
			.append(" (runDate=").append(runDate).append(")\n")
			.append("- 실거래가: 조회 ").append(formatNum(tradeRead))
			.append(" / 저장 ").append(formatNum(tradeWrite))
			.append(" / 스킵 ").append(formatNum(tradeSkip)).append("\n")
			.append("- 계산: trend 업데이트 ").append(formatNum(trendUpdated)).append("건\n")
			.append("- 메일: 대상 ").append(formatNum(mailTarget))
			.append(" / 성공 ").append(formatNum(mailSent))
			.append(" / 실패 ").append(formatNum(mailFailed)).append("\n")
			.append("- 파티션: ").append(threads).append(" threads / ")
			.append(partitions).append(" partitions\n")
			.append("- 소요시간: ").append(duration);

		if (!success) {
			Throwable t = jobExecution.getAllFailureExceptions().stream().findFirst().orElse(null);
			if (t != null) {
				sb.append("\n- 에러: ").append(t.getClass().getSimpleName())
					.append(" - ").append(safeMsg(t.getMessage()));
			}
		}
		return sb.toString();
	}

	private static Counters sumStep(Collection<StepExecution> steps, String stepNameContains) {
		long read = 0, write = 0, skip = 0;
		for (StepExecution se : steps) {
			if (!se.getStepName().contains(stepNameContains))
				continue;
			read += se.getReadCount();
			write += se.getWriteCount();
			skip += se.getFilterCount() + se.getSkipCount();
		}
		return new Counters(read, write, skip);
	}

	private static int sumCtxInt(Collection<StepExecution> steps, String key) {
		int sum = 0;
		for (StepExecution se : steps) {
			if (se.getExecutionContext().containsKey(key)) {
				sum += se.getExecutionContext().getInt(key);
			}
		}
		return sum;
	}

	private static String format(Duration d) {
		long s = d.getSeconds();
		long hh = s / 3600;
		long mm = (s % 3600) / 60;
		long ss = s % 60;
		return String.format("%02d:%02d:%02d", hh, mm, ss);
	}

	private static String formatNum(long n) {
		return String.format("%,d", n);
	}

	private static String safeMsg(String msg) {
		if (msg == null)
			return "-";
		return msg.length() > 120 ? msg.substring(0, 120) + "..." : msg;
	}

	private record Counters(long read, long write, long filterOrSkip) {
	}
}
