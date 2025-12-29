package com.home.infrastructure.batch.trade;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class TradeMonthSggPartitioner implements Partitioner {

	private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMM");

	private final List<String> sggCodes;
	private final YearMonth from;
	private final YearMonth to;
	private final int groupSize;

	public TradeMonthSggPartitioner(
		List<String> sggCodes,
		YearMonth from,
		YearMonth to,
		int groupSize
	) {
		this.sggCodes = sggCodes;
		this.from = from;
		this.to = to;
		this.groupSize = groupSize;
	}

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<>();
		int idx = 0;

		for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
			for (int i = 0; i < sggCodes.size(); i += groupSize) {

				List<String> chunk =
					new ArrayList<>(sggCodes.subList(
						i,
						Math.min(i + groupSize, sggCodes.size())
					));

				ExecutionContext ec = new ExecutionContext();
				ec.putString("dealYmd", ym.format(F));
				ec.put("sggCodes", chunk);

				map.put("partition-" + (idx++), ec);
			}
		}
		return map;
	}
}
