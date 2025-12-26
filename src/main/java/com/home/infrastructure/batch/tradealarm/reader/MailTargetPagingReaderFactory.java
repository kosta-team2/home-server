package com.home.infrastructure.batch.tradealarm.reader;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;

import com.home.infrastructure.batch.tradealarm.dto.MailTargetRow;

public class MailTargetPagingReaderFactory {

	public static JdbcPagingItemReader<MailTargetRow> create(DataSource dataSource, LocalDate batchDate) {
		JdbcPagingItemReader<MailTargetRow> reader = new JdbcPagingItemReader<>();
		reader.setName("mailTargetReader");
		reader.setDataSource(dataSource);
		reader.setPageSize(200);

		PostgresPagingQueryProvider qp = new PostgresPagingQueryProvider();
		qp.setSelectClause("select id, user_id, parcel_id, email, complex_name, try_count");
		qp.setFromClause("from mail_target");
		qp.setWhereClause("""
			where batch_date = :batchDate
			  and mail_type = 'TRADE_UPDATE'
			  and status in ('PENDING','FAILED')
			  and try_count < 3
			""");
		qp.setSortKeys(Map.of("id", Order.ASCENDING));
		reader.setQueryProvider(qp);

		reader.setParameterValues(Map.of("batchDate", batchDate));

		reader.setRowMapper((ResultSet rs, int rowNum) -> new MailTargetRow(
			rs.getLong("id"),
			rs.getLong("user_id"),
			rs.getLong("parcel_id"),
			rs.getString("email"),
			rs.getString("complex_name"),
			rs.getInt("try_count")
		));

		return reader;
	}
}
