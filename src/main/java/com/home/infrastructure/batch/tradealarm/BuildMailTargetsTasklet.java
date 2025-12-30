package com.home.infrastructure.batch.tradealarm;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class BuildMailTargetsTasklet implements Tasklet {

	private final NamedParameterJdbcTemplate olapJdbc; // trade/complex 조회 + mail_target insert (배치DB)
	private final NamedParameterJdbcTemplate oltpJdbc; // favorite_parcel/users 조회 (웹DB)

	public BuildMailTargetsTasklet(
		NamedParameterJdbcTemplate olapJdbc,
		NamedParameterJdbcTemplate oltpJdbc
	) {
		this.olapJdbc = olapJdbc;
		this.oltpJdbc = oltpJdbc;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

		String runDateStr = (String) chunkContext.getStepContext().getJobParameters().get("runDate");
		LocalDate runDate = (runDateStr == null) ? LocalDate.now() : LocalDate.parse(runDateStr);

		LocalDateTime from = runDate.atStartOfDay();
		LocalDateTime to = runDate.plusDays(1).atStartOfDay();

		List<Long> parcelIds = olapJdbc.queryForList("""
			select distinct c.parcel_id
			  from trade t
			  join complex c on c.complex_pk = t.complex_pk
			 where t.created_at >= :fromTs
			   and t.created_at <  :toTs
			   and t.deleted_at is null
			   and c.deleted_at is null
			   and c.parcel_id is not null
			""", Map.of(
			"fromTs", Timestamp.valueOf(from),
			"toTs", Timestamp.valueOf(to)
		), Long.class);

		if (parcelIds.isEmpty()) {
			putJobCtxLong(chunkContext, "mail.target", 0L);
			contribution.incrementWriteCount(0);
			return RepeatStatus.FINISHED;
		}

		List<Map<String, Object>> targets = oltpJdbc.queryForList("""
			select
			  u.id as user_id,
			  fp.parcel_id::bigint as parcel_id,
			  u.user_email as email,
			  fp.complex_name as complex_name
			from favorite_parcel fp
			join users u
			  on u.id = fp.user_id::bigint
			 and u.deleted_at is null
			where fp.alarm_enabled = true
			  and fp.deleted_at is null
			  and fp.parcel_id::bigint in (:parcelIds)
			""", Map.of("parcelIds", parcelIds));

		if (targets.isEmpty()) {
			putJobCtxLong(chunkContext, "mail.target", 0L);
			contribution.incrementWriteCount(0);
			return RepeatStatus.FINISHED;
		}

		// 3) OLAP: mail_target에 batch insert
		String insertSql = """
			insert into mail_target(batch_date, mail_type, user_id, parcel_id, email, complex_name, status)
			values (:batchDate, 'TRADE_UPDATE', :userId, :parcelId, :email, :complexName, 'PENDING')
			on conflict (batch_date, mail_type, user_id, parcel_id) do nothing
			""";

		SqlParameterSource[] batchParams = targets.stream()
			.map(row -> new MapSqlParameterSource()
				.addValue("batchDate", runDate)
				.addValue("userId", ((Number) row.get("user_id")).longValue())
				.addValue("parcelId", ((Number) row.get("parcel_id")).longValue())
				.addValue("email", (String) row.get("email"))
				.addValue("complexName", (String) row.get("complex_name"))
			)
			.toArray(SqlParameterSource[]::new);

		int[] counts = olapJdbc.batchUpdate(insertSql, batchParams);

		long inserted = 0;
		for (int c : counts) inserted += Math.max(c, 0); // conflict면 0

		putJobCtxLong(chunkContext, "mail.target", inserted);
		contribution.incrementWriteCount((int) Math.min(Integer.MAX_VALUE, inserted));

		return RepeatStatus.FINISHED;
	}

	private static void putJobCtxLong(ChunkContext chunkContext, String key, long value) {
		var jobCtx = chunkContext.getStepContext()
			.getStepExecution()
			.getJobExecution()
			.getExecutionContext();
		jobCtx.putLong(key, value);
	}
}
