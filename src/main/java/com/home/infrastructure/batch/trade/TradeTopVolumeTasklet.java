package com.home.infrastructure.batch.trade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class TradeTopVolumeTasklet implements Tasklet {

	private final NamedParameterJdbcTemplate olapJdbc;
	private final NamedParameterJdbcTemplate oltpJdbc;

	public TradeTopVolumeTasklet(
		@Qualifier("olapJdbc") NamedParameterJdbcTemplate olapJdbc,
		@Qualifier("oltpJdbc") NamedParameterJdbcTemplate oltpJdbc
	) {
		this.olapJdbc = olapJdbc;
		this.oltpJdbc = oltpJdbc;
	}

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {
		LocalDate today = LocalDate.now();

		List<TopVolumeRow> rows = olapJdbc.query(
			"""
			select
			    region_id,
			    rank,
			    complex_id,
			    deal_count
			from (
			    select
			        r.id as region_id,
			        c.id as complex_id,
			        count(*) as deal_count,
			        row_number() over (
			            partition by r.id
			            order by count(*) desc
			        ) as rank
			    from trade t
			    join complex c on t.complex_pk = c.complex_pk
			    join parcel p on c.parcel_id = p.id
			    join region r on p.region_id = r.id
			    where t.deal_date >= :fromDate
			    group by r.id, c.id
			) ranked
			where rank <= 10
			order by region_id, rank
			""",
			Map.of("fromDate", today.minusDays(30)),
			(rs, i) -> new TopVolumeRow(
				rs.getLong("region_id"),
				rs.getInt("rank"),
				rs.getLong("complex_id"),
				rs.getLong("deal_count")
			)
		);

		oltpJdbc.getJdbcTemplate()
			.execute("truncate table trade_top_volume_30d");

		for (TopVolumeRow row : rows) {
			oltpJdbc.update(
				"""
				insert into trade_top_volume_30d
				    (region_id, rank, complex_id, deal_count)
				values
				    (:regionId, :rank, :complexId, :dealCount)
				""",
				Map.of(
					"regionId", row.regionId(),
					"rank", row.rank(),
					"complexId", row.complexId(),
					"dealCount", row.dealCount()
				)
			);
		}

		log.info("[BATCH][TOP_VOLUME_30D] inserted rows={}", rows.size());
		return RepeatStatus.FINISHED;
	}

	private record TopVolumeRow(
		long regionId,
		int rank,
		long complexId,
		long dealCount
	) {}
}
