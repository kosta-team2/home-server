package com.home.infrastructure.batch.trade;

import java.time.LocalDate;
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

	private final NamedParameterJdbcTemplate oltpJdbc;

	public TradeTopPriceTasklet(
		@Qualifier("oltpJdbc") NamedParameterJdbcTemplate oltpJdbc
	) {
		this.oltpJdbc = oltpJdbc;
	}

	@Override
	public RepeatStatus execute(
		StepContribution contribution,
		ChunkContext chunkContext
	) {

		LocalDate today = LocalDate.now();

		oltpJdbc.getJdbcTemplate()
			.execute("truncate table trade_top_price_30d");

		int inserted = oltpJdbc.update(
			"""
				insert into trade_top_price_30d (rank, complex_id, max_price)
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
				    join complex c on t.complex_id = c.id
				    where t.deal_date >= :fromDate
				    group by c.id
				) ranked
				where rank <= 10
				""",
			Map.of(
				"fromDate", today.minusDays(30)
			)
		);

		log.info("[BATCH][TOP_PRICE_30D] inserted rows={}", inserted);
		return RepeatStatus.FINISHED;
	}
}
