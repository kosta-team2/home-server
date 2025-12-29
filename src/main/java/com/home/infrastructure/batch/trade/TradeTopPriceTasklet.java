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
public class TradeTopPriceTasklet implements Tasklet {

	private final NamedParameterJdbcTemplate olapJdbc;
	private final NamedParameterJdbcTemplate oltpJdbc;

	public TradeTopPriceTasklet(
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

		List<TopPriceRow> rows = olapJdbc.query(
			"""
			select
			    rank,
			    complex_id,
			    max_price
			from (
			    select
			        c.id as complex_id,
			        max(t.deal_amount) as max_price,
			        row_number() over (
			            order by max(t.deal_amount) desc
			        ) as rank
			    from trade t
			    join complex c on t.complex_pk = c.complex_pk
			    where t.deal_date >= :fromDate
			    group by c.id
			) ranked
			where rank <= 10
			order by rank
			""",
			Map.of("fromDate", today.minusDays(30)),
			(rs, i) -> new TopPriceRow(
				rs.getInt("rank"),
				rs.getLong("complex_id"),
				rs.getLong("max_price")
			)
		);

		oltpJdbc.getJdbcTemplate()
			.execute("truncate table trade_top_price_30d");

		for (TopPriceRow row : rows) {
			oltpJdbc.update(
				"""
				insert into trade_top_price_30d
				    (rank, complex_id, max_price)
				values
				    (:rank, :complexId, :maxPrice)
				""",
				Map.of(
					"rank", row.rank(),
					"complexId", row.complexId(),
					"maxPrice", row.maxPrice()
				)
			);
		}

		log.info("[BATCH][TOP_PRICE_30D] inserted rows={}", rows.size());
		return RepeatStatus.FINISHED;
	}

	private record TopPriceRow(
		int rank,
		long complexId,
		long maxPrice
	) {}
}
