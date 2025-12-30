package com.home.infrastructure.batch.tradealarm;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class BuildMailTargetsTasklet implements Tasklet {

	private final NamedParameterJdbcTemplate jdbc;

	public BuildMailTargetsTasklet(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		String runDateStr = (String) chunkContext.getStepContext().getJobParameters().get("runDate");
		LocalDate runDate = (runDateStr == null) ? LocalDate.now() : LocalDate.parse(runDateStr);

		LocalDateTime from = runDate.atStartOfDay();
		LocalDateTime to = runDate.plusDays(1).atStartOfDay();

		String sql = """
			insert into mail_target(batch_date, mail_type, user_id, parcel_id, email, complex_name, status)
			select
			  :batchDate as batch_date,
			  'TRADE_UPDATE' as mail_type,
			  u.id as user_id,
			  fp.parcel_id as parcel_id,
			  u.user_email as email,
			  fp.complex_name as complex_name,
			  'PENDING' as status
			from (
			  select distinct c.parcel_id
			  from trade t
			  join complex c on c.id = t.complex_pk
			  where t.created_at >= :fromTs
			    and t.created_at <  :toTs
			    and t.deleted_at is null
			    and c.deleted_at is null
			    and c.parcel_id is not null
			) p
			join favorite_parcel fp
			  on fp.parcel_id = p.parcel_id
			 and fp.alarm_enabled = true
			 and fp.deleted_at is null
			join users u
			  on u.id = fp.user_id
			 and u.deleted_at is null
			on conflict (batch_date, mail_type, user_id, parcel_id) do nothing
			""";

		int inserted = jdbc.update(sql, Map.of(
			"batchDate", runDate,
			"fromTs", Timestamp.valueOf(from),
			"toTs", Timestamp.valueOf(to)
		));

		contribution.incrementWriteCount(inserted);
		return RepeatStatus.FINISHED;
	}
}

