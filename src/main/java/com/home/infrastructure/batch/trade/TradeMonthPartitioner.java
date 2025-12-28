package com.home.infrastructure.batch.trade;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class TradeMonthPartitioner implements Partitioner {

	private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMM");

	private final YearMonth from;
	private final YearMonth to;

	public TradeMonthPartitioner(YearMonth from, YearMonth to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>();

		YearMonth cur = from;
		int idx = 0;
		while (!cur.isAfter(to)) {
			ExecutionContext ec = new ExecutionContext();
			ec.putString("dealYmd", cur.format(F));
			map.put("month-" + (idx++), ec);
			cur = cur.plusMonths(1);
		}
		return map;
	}
}
