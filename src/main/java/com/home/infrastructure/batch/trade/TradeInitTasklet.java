package com.home.infrastructure.batch.trade;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.home.infrastructure.batch.trade.dto.TradeRow;
import com.home.infrastructure.external.apis.ApisClient;
import com.home.infrastructure.external.apis.dto.ApisAptTradeDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class TradeInitTasklet implements Tasklet {

	private static final int PAGE_SIZE = 1000;
	private static final int BATCH_SIZE = 2000;

	private static final String EC_READ = "trade.read";
	private static final String EC_INSERTED = "trade.inserted";
	private static final String EC_SKIPPED = "trade.skipped";

	private final ApisClient apisClient;
	private final TradeBulkWriter tradeBulkWriter;
	private final NamedParameterJdbcTemplate olapJdbc;

	private final Map<String, String> pnuToUniqueComplexPkCache = new ConcurrentHashMap<>();
	private final Map<String, Boolean> pnuMissCache = new ConcurrentHashMap<>();

	private final Map<String, String> pnuNameToComplexPkCache = new ConcurrentHashMap<>();

	private final Map<String, String> aptSeqToComplexPkCache = new ConcurrentHashMap<>();
	private final Map<String, Boolean> aptSeqMissCache = new ConcurrentHashMap<>();

	private final Set<String> ambiguousPnuLogged = ConcurrentHashMap.newKeySet();

	@Value("#{stepExecutionContext['dealYmd']}")
	private String dealYmd;

	public TradeInitTasklet(
		ApisClient apisClient,
		TradeBulkWriter tradeBulkWriter,
		@Qualifier("olapJdbc") NamedParameterJdbcTemplate olapJdbc
	) {
		this.apisClient = apisClient;
		this.tradeBulkWriter = tradeBulkWriter;
		this.olapJdbc = olapJdbc;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		ExecutionContext ec = chunkContext.getStepContext().getStepExecution().getExecutionContext();

		long read = ec.containsKey(EC_READ) ? ec.getLong(EC_READ) : 0L;
		long inserted = ec.containsKey(EC_INSERTED) ? ec.getLong(EC_INSERTED) : 0L;
		long skipped = ec.containsKey(EC_SKIPPED) ? ec.getLong(EC_SKIPPED) : 0L;

		List<String> sggCodes = olapJdbc.queryForList(
			"select sgg_code from region where level = 'SIGUNGU'",
			Map.of(),
			String.class
		);

		List<TradeRow> batch = new ArrayList<>(BATCH_SIZE);

		for (String sggCode : sggCodes) {
			int pageNo = 1;

			while (true) {
				List<ApisAptTradeDto> apisList = apisClient.getAptTrade(pageNo, PAGE_SIZE, sggCode, dealYmd).toApisDto();
				if (apisList == null || apisList.isEmpty()) break;

				for (ApisAptTradeDto dto : apisList) {
					read++;

					ResolvedComplex resolved = resolveComplex(dto);
					if (resolved == null) {
						skipped++;
						continue;
					}

					long amount = parseAmount(dto.dealAmount());
					if (amount <= 0) {
						skipped++;
						continue;
					}

					LocalDate dealDate = LocalDate.of(dto.dealYear(), dto.dealMonth(), dto.dealDay());

					if (hasText(dto.aptSeq())) {
						AptSeqUpdateResult u = upsertComplexAptSeq(resolved.complexPk(), dto.aptSeq(), dto);
						if (u.changed()) {
							if (hasText(u.oldAptSeq())) aptSeqToComplexPkCache.remove(u.oldAptSeq());
						}
						aptSeqToComplexPkCache.put(dto.aptSeq(), resolved.complexPk());
						aptSeqMissCache.remove(dto.aptSeq());
					}

					batch.add(new TradeRow(
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

					if (batch.size() >= BATCH_SIZE) {
						var r = tradeBulkWriter.insertIgnore(batch);
						inserted += r.inserted();
						skipped += r.skipped();
						batch.clear();
					}
				}

				if (apisList.size() < PAGE_SIZE) break;
				pageNo++;
			}
		}

		if (!batch.isEmpty()) {
			var r = tradeBulkWriter.insertIgnore(batch);
			inserted += r.inserted();
			skipped += r.skipped();
			batch.clear();
		}

		ec.putLong(EC_READ, read);
		ec.putLong(EC_INSERTED, inserted);
		ec.putLong(EC_SKIPPED, skipped);

		log.info("[BATCH][tradeInit] dealYmd={}, read={}, inserted={}, skipped={}", dealYmd, read, inserted, skipped);
		return RepeatStatus.FINISHED;
	}

	private ResolvedComplex resolveComplex(ApisAptTradeDto dto) {
		// 1) aptSeq direct
		String aptSeq = dto.aptSeq();
		if (hasText(aptSeq)) {
			String cached = aptSeqToComplexPkCache.get(aptSeq);
			if (cached != null) return new ResolvedComplex(cached, ResolvePath.APTSEQ);

			if (!aptSeqMissCache.containsKey(aptSeq)) {
				List<String> byAptSeq = olapJdbc.queryForList(
					"select complex_pk from complex where apt_seq = :aptSeq limit 1",
					Map.of("aptSeq", aptSeq),
					String.class
				);
				if (!byAptSeq.isEmpty()) {
					String complexPk = byAptSeq.get(0);
					aptSeqToComplexPkCache.put(aptSeq, complexPk);
					return new ResolvedComplex(complexPk, ResolvePath.APTSEQ);
				}
				aptSeqMissCache.put(aptSeq, true);
			}
		}

		String pnu = buildPnu(dto);
		if (hasText(pnu)) {
			String uniqueCached = pnuToUniqueComplexPkCache.get(pnu);
			if (uniqueCached != null) return new ResolvedComplex(uniqueCached, ResolvePath.PNU_UNIQUE);

			if (hasText(dto.aptNm())) {
				String key = pnuNameKey(pnu, dto.aptNm());
				String cached = pnuNameToComplexPkCache.get(key);
				if (cached != null) return new ResolvedComplex(cached, ResolvePath.PNU_NAME);
			}

			if (!pnuMissCache.containsKey(pnu)) {
				List<ComplexCandidate> cands = queryCandidatesByPnu(pnu);

				if (cands.isEmpty()) {
					pnuMissCache.put(pnu, true);
				} else if (cands.size() == 1) {
					String complexPk = cands.get(0).complexPk();
					pnuToUniqueComplexPkCache.put(pnu, complexPk);
					return new ResolvedComplex(complexPk, ResolvePath.PNU_UNIQUE);
				} else {
					ComplexCandidate chosen = chooseByName(dto.aptNm(), cands);
					if (chosen != null) {
						if (hasText(dto.aptNm())) {
							pnuNameToComplexPkCache.put(pnuNameKey(pnu, dto.aptNm()), chosen.complexPk());
						}
						return new ResolvedComplex(chosen.complexPk(), ResolvePath.PNU_NAME);
					}

					String logKey = pnu + "|" + dealYmd;
					if (ambiguousPnuLogged.add(logKey)) {
						log.warn("[BATCH][tradeInit] PNU 후보 다수 & 이름으로도 단일 매칭 실패 -> 저장 스킵. pnu={}, aptNm={}, candidates={}",
							pnu, dto.aptNm(), cands.size());
					}
				}
			}
		}

		if (hasText(dto.aptNm()) && hasText(dto.umdCd()) && dto.umdCd().length() == 5) {
			List<ComplexNameCandidate> candidates = olapJdbc.query(
				"""
					select c.complex_pk as complexPk,
					       coalesce(c.pnu, p.pnu) as pnu
					  from complex c
					  left join parcel p on p.id = c.parcel_id
					 where c.trade_name = :aptNm or c.name = :aptNm
					""",
				Map.of("aptNm", dto.aptNm()),
				(rs, rowNum) -> new ComplexNameCandidate(
					rs.getString("complexPk"),
					rs.getString("pnu")
				)
			);

			List<ComplexNameCandidate> filtered = candidates.stream()
				.filter(c -> c.pnu() != null && c.pnu().length() >= 10)
				.filter(c -> c.pnu().substring(5, 10).equals(dto.umdCd()))
				.toList();

			if (filtered.size() == 1) {
				return new ResolvedComplex(filtered.get(0).complexPk(), ResolvePath.NAME_UMD);
			}
		}

		return null;
	}

	private List<ComplexCandidate> queryCandidatesByPnu(String pnu) {
		return olapJdbc.query(
			"""
				select c.complex_pk as complexPk,
				       c.apt_seq as aptSeq,
				       c.trade_name as tradeName,
				       c.name as name
				  from complex c
				  left join parcel p on p.id = c.parcel_id
				 where c.pnu = :pnu or p.pnu = :pnu
				""",
			Map.of("pnu", pnu),
			(rs, rowNum) -> new ComplexCandidate(
				rs.getString("complexPk"),
				rs.getString("aptSeq"),
				rs.getString("tradeName"),
				rs.getString("name")
			)
		);
	}

	private ComplexCandidate chooseByName(String dtoAptNm, List<ComplexCandidate> cands) {
		if (!hasText(dtoAptNm)) return null;

		String target = normalizeName(dtoAptNm);

		int bestScore = 0;
		ComplexCandidate best = null;
		boolean tie = false;

		for (ComplexCandidate c : cands) {
			int score = scoreName(target, c);
			if (score == 0) continue;

			if (score > bestScore) {
				bestScore = score;
				best = c;
				tie = false;
			} else if (score == bestScore) {
				tie = true;
			}
		}

		if (bestScore == 0 || tie) return null;
		return best;
	}

	private int scoreName(String target, ComplexCandidate c) {
		String t1 = normalizeName(c.tradeName());
		String t2 = normalizeName(c.name());

		if (target.equals(t1) || target.equals(t2)) return 3;

		if ((hasText(t1) && (t1.contains(target) || target.contains(t1))) ||
			(hasText(t2) && (t2.contains(target) || target.contains(t2)))) return 2;

		return 0;
	}

	private String normalizeName(String s) {
		if (!hasText(s)) return "";
		return s.replaceAll("\\s+", "")
			.replaceAll("[()\\[\\]{}.,·\\-_/]", "")
			.toLowerCase(Locale.ROOT)
			.trim();
	}

	private String pnuNameKey(String pnu, String aptNm) {
		return pnu + "|" + normalizeName(aptNm);
	}

	private AptSeqUpdateResult upsertComplexAptSeq(String complexPk, String newAptSeq, ApisAptTradeDto dto) {
		String old;
		try {
			old = olapJdbc.queryForObject(
				"select apt_seq from complex where complex_pk = :complexPk",
				Map.of("complexPk", complexPk),
				String.class
			);
		} catch (EmptyResultDataAccessException e) {
			return new AptSeqUpdateResult(null, false);
		}

		if (Objects.equals(old, newAptSeq)) {
			return new AptSeqUpdateResult(old, false);
		}

		int updated = olapJdbc.update(
			"""
				update complex
				   set apt_seq = :aptSeq
				 where complex_pk = :complexPk
				""",
			Map.of("complexPk", complexPk, "aptSeq", newAptSeq)
		);

		if (updated > 0) {
			if (!hasText(old)) {
				log.info("[BATCH][tradeInit][APTSEQ_SET] sggCd={}, umdCd={}, aptNm={}, complexPk={}, newAptSeq={}",
					dto.sggCd(), dto.umdCd(), dto.aptNm(), complexPk, newAptSeq);
			} else {
				log.info("[BATCH][tradeInit][APTSEQ_CHANGED] sggCd={}, umdCd={}, aptNm={}, complexPk={}, oldAptSeq={}, newAptSeq={}",
					dto.sggCd(), dto.umdCd(), dto.aptNm(), complexPk, old, newAptSeq);
			}
			return new AptSeqUpdateResult(old, true);
		}

		return new AptSeqUpdateResult(old, false);
	}

	private String buildPnu(ApisAptTradeDto dto) {
		if (!hasText(dto.sggCd()) || !hasText(dto.umdCd())) return null;
		if (dto.sggCd().length() != 5 || dto.umdCd().length() != 5) return null;

		String bon = firstNonBlank(dto.bonbun(), parseBonbunFromJibun(dto.jibun()));
		String bu = firstNonBlank(dto.bubun(), parseBubunFromJibun(dto.jibun()));
		if (!hasText(bon)) return null;

		String land = inferLand(dto.jibun());
		String bon4 = pad4Digits(bon);
		String bu4 = pad4Digits(hasText(bu) ? bu : "0");

		return dto.sggCd() + dto.umdCd() + land + bon4 + bu4;
	}

	private String inferLand(String jibun) {
		if (hasText(jibun) && jibun.contains("산")) return "2";
		return "1";
	}

	private String parseBonbunFromJibun(String jibun) {
		if (!hasText(jibun)) return null;
		String cleaned = jibun.replace("산", "").trim();
		String[] parts = cleaned.split("-");
		return parts.length >= 1 ? digitsOnly(parts[0]) : null;
	}

	private String parseBubunFromJibun(String jibun) {
		if (!hasText(jibun)) return null;
		String cleaned = jibun.replace("산", "").trim();
		String[] parts = cleaned.split("-");
		return parts.length >= 2 ? digitsOnly(parts[1]) : null;
	}

	private String digitsOnly(String s) {
		if (!hasText(s)) return null;
		String d = s.replaceAll("[^0-9]", "");
		return d.isBlank() ? null : d;
	}

	private String pad4Digits(String s) {
		String d = digitsOnly(s);
		if (d == null) return null;
		if (d.length() > 4) return d.substring(d.length() - 4);
		return "0".repeat(4 - d.length()) + d;
	}

	private String firstNonBlank(String a, String b) {
		if (hasText(a)) return a;
		if (hasText(b)) return b;
		return null;
	}

	private long parseAmount(String s) {
		if (!hasText(s)) return 0L;
		return Long.parseLong(s.replace(",", "").trim());
	}

	private boolean hasText(String s) {
		return s != null && !s.isBlank();
	}

	private record ResolvedComplex(String complexPk, ResolvePath path) {}
	private enum ResolvePath { APTSEQ, PNU_UNIQUE, PNU_NAME, NAME_UMD }
	private record ComplexCandidate(String complexPk, String aptSeq, String tradeName, String name) {}
	private record ComplexNameCandidate(String complexPk, String pnu) {}
	private record AptSeqUpdateResult(String oldAptSeq, boolean changed) {}
}
