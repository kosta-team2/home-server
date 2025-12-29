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
public class TradeTrendTasklet implements Tasklet {

	private final NamedParameterJdbcTemplate olapJdbc;
	private final NamedParameterJdbcTemplate oltpJdbc;

	public TradeTrendTasklet(
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

		List<TrendRow> rows = olapJdbc.query(
			"""
			with base_trade as (
			    select
			        t.deal_date,
			        (t.deal_amount / t.excl_area) as unit_price,
			        r.id as emd_id,
			        r.parent_id as sgg_id,
			        rp.parent_id as sido_id
			    from trade t
			    join complex c on t.complex_pk = c.complex_pk
			    join parcel p on c.parcel_id = p.id
			    join region r on p.region_id = r.id
			    left join region rp on r.parent_id = rp.id
			    where t.deal_date >= :fromDate
			      and t.excl_area > 0
			),
			region_trade as (
			    select emd_id as region_id, deal_date, unit_price from base_trade
			    union all
			    select sgg_id, deal_date, unit_price from base_trade where sgg_id is not null
			    union all
			    select sido_id, deal_date, unit_price from base_trade where sido_id is not null
			)
			select
			    region_id,
			    avg(case
			        when deal_date >= :recentFrom then unit_price
			    end) as avg_recent,
			    avg(case
			        when deal_date < :recentFrom
			         and deal_date >= :prevFrom then unit_price
			    end) as avg_prev
			from region_trade
			group by region_id
			""",
			Map.of(
				"fromDate", today.minusDays(60),
				"recentFrom", today.minusDays(30),
				"prevFrom", today.minusDays(60)
			),
			(rs, i) -> new TrendRow(
				rs.getLong("region_id"),
				rs.getDouble("avg_recent"),
				rs.getDouble("avg_prev")
			)
		);

		int updated = 0;

		for (TrendRow row : rows) {
			if (row.avgPrev() <= 0 || row.avgRecent() <= 0) continue;

			double trend = (row.avgRecent() - row.avgPrev()) / row.avgPrev();

			oltpJdbc.update(
				"""
				update region
				   set trend_30d = :trend
				 where id = :regionId
				""",
				Map.of(
					"regionId", row.regionId(),
					"trend", trend
				)
			);
			updated++;
		}

		log.info("[BATCH][TRADE_TREND] region updated={}", updated);
		return RepeatStatus.FINISHED;
	}

	private record TrendRow(
		long regionId,
		double avgRecent,
		double avgPrev
	) {}
}
