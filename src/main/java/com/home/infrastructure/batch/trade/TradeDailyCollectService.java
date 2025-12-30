package com.home.infrastructure.batch.trade;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.home.infrastructure.batch.trade.dto.TradeRow;
import com.home.infrastructure.external.apis.ApisClient;
import com.home.infrastructure.external.apis.dto.ApisAptTradeDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TradeDailyCollectService {

	private static final int PAGE_SIZE = 1000;
	private static final int BATCH_SIZE = 2000;

	private final ApisClient apisClient;
	private final TradeBulkWriter tradeBulkWriter;
	private final NamedParameterJdbcTemplate olapJdbc;
	private final ComplexResolveService complexResolveService;

	public TradeDailyCollectService(
		ApisClient apisClient,
		TradeBulkWriter tradeBulkWriter,
		NamedParameterJdbcTemplate olapJdbc,
		ComplexResolveService complexResolveService
	) {
		this.apisClient = apisClient;
		this.tradeBulkWriter = tradeBulkWriter;
		this.olapJdbc = olapJdbc;
		this.complexResolveService = complexResolveService;
	}

	public long collect(LocalDate targetDate) {

		YearMonth ym = YearMonth.from(targetDate);
		String dealYmd = ym.format(DateTimeFormatter.ofPattern("yyyyMM"));

		List<String> sggCodes = loadSggCodes();

		List<TradeRow> buffer = new ArrayList<>(BATCH_SIZE);
		long insertedTotal = 0;

		for (String sggCode : sggCodes) {
			int page = 1;

			while (true) {
				List<ApisAptTradeDto> list =
					apisClient.getAptTrade(page, PAGE_SIZE, sggCode, dealYmd)
						.toApisDto();

				if (list == null || list.isEmpty()) break;

				for (ApisAptTradeDto dto : list) {

					LocalDate dealDate = LocalDate.of(
						dto.dealYear(),
						dto.dealMonth(),
						dto.dealDay()
					);

					ComplexResolveService.ResolvedComplex resolved =
						complexResolveService.resolve(dto, dealYmd);

					if (resolved == null) continue;

					long amount = parseAmount(dto.dealAmount());
					if (amount <= 0) continue;

					buffer.add(new TradeRow(
						dealDate,
						dto.aptDong(),
						amount,
						dto.floor(),
						dto.exclArea(),
						resolved.complexPk(),
						dto.aptSeq(),
						"RTMS",
						null
					));

					if (buffer.size() >= BATCH_SIZE) {
						var r = tradeBulkWriter.insertIgnore(buffer);
						insertedTotal += r.inserted();
						buffer.clear();
					}
				}

				if (list.size() < PAGE_SIZE) break;
				page++;
			}
		}

		if (!buffer.isEmpty()) {
			var r = tradeBulkWriter.insertIgnore(buffer);
			insertedTotal += r.inserted();
		}

		log.info(
			"[DAILY][COLLECT] date={}, inserted={}",
			targetDate, insertedTotal
		);

		return insertedTotal;
	}

	private List<String> loadSggCodes() {
		return olapJdbc.queryForList(
			"select sgg_code from region where level = :level",
			Map.of("level", "SIGUNGU"),
			String.class
		);
	}

	private long parseAmount(String s) {
		if (s == null || s.isBlank()) return 0L;
		return Long.parseLong(s.replace(",", "").trim());
	}
}
