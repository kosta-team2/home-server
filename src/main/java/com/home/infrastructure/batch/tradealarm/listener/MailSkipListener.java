package com.home.infrastructure.batch.tradealarm.listener;

import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import com.home.infrastructure.batch.tradealarm.dto.MailTargetRow;

public class MailSkipListener implements SkipListener<MailTargetRow, MailTargetRow> {

	private final JdbcTemplate jdbc;

	public MailSkipListener(@Qualifier("olapJdbcTemplate") JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void onSkipInProcess(MailTargetRow item, Throwable t) {
		jdbc.update("""
			update mail_target
			   set status = 'FAILED',
			       try_count = try_count + 1,
			       last_error = ?
			 where id = ?
			""", truncate(t.getMessage(), 2000), item.id());

		var stepContext = org.springframework.batch.core.scope.context.StepSynchronizationManager.getContext();
		if (stepContext != null) {
			var jobCtx = stepContext
				.getStepExecution()
				.getJobExecution()
				.getExecutionContext();

			jobCtx.putLong(
				"mail.failed",
				jobCtx.getLong("mail.failed", 0L) + 1
			);
		}
	}

	private String truncate(String s, int max) {
		if (s == null)
			return null;
		return s.length() <= max ? s : s.substring(0, max);
	}
}
