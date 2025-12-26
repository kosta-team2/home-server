package com.home.infrastructure.batch.tradealarm.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.home.infrastructure.batch.tradealarm.dto.MailTargetRow;

public class MailStatusSuccessWriter implements ItemWriter<MailTargetRow> {

	private final JdbcTemplate jdbc;

	public MailStatusSuccessWriter(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void write(Chunk<? extends MailTargetRow> chunk) {
		for (MailTargetRow row : chunk) {
			jdbc.update("""
				update mail_target
				   set status = 'SENT',
				       sent_at = now(),
				       try_count = try_count + 1,
				       last_error = null
				 where id = ?
				""", row.id());
		}
	}
}
