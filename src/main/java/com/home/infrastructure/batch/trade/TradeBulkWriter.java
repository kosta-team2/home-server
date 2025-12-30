package com.home.infrastructure.batch.trade;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.home.infrastructure.batch.trade.dto.Result;
import com.home.infrastructure.batch.trade.dto.TradeRow;

import lombok.RequiredArgsConstructor;

@Component
public class TradeBulkWriter {

	private final JdbcTemplate jdbcTemplate;

	public TradeBulkWriter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private static final String SQL = """
		INSERT INTO trade
		  (deal_date, apt_dong, deal_amount, floor, excl_area, complex_pk, apt_seq, source, source_key)
		VALUES
		  (?, ?, ?, ?, ?, ?, ?, ?, ?)
		ON CONFLICT ON CONSTRAINT uq_trade_nk DO NOTHING
		""";

	public Result insertIgnore(List<TradeRow> rows) {
		if (rows == null || rows.isEmpty()) {
			return new Result(0, 0, 0);
		}

		int[] counts = jdbcTemplate.batchUpdate(SQL, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
				TradeRow r = rows.get(i);
				ps.setObject(1, r.dealDate());
				ps.setString(2, r.aptDong());
				ps.setLong(3, r.dealAmount());
				if (r.floor() == null)
					ps.setNull(4, java.sql.Types.INTEGER);
				else
					ps.setInt(4, r.floor());
				if (r.exclArea() == null)
					ps.setNull(5, java.sql.Types.DOUBLE);
				else
					ps.setDouble(5, r.exclArea());
				ps.setString(6, r.complexPk());
				ps.setString(7, r.aptSeq());
				ps.setString(8, r.source() == null ? "RTMS" : r.source());
				ps.setString(9, r.sourceKey());
			}

			@Override
			public int getBatchSize() {
				return rows.size();
			}
		});

		long inserted = 0;
		for (int c : counts) {
			if (c > 0)
				inserted += c;
			else if (c == Statement.SUCCESS_NO_INFO) {
				inserted += 1;
			}
		}

		long attempted = rows.size();
		long skipped = attempted - inserted;
		return new Result(attempted, inserted, skipped);
	}
}
