package com.home.infrastructure.batch.trade;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.home.infrastructure.external.apis.dto.ApisAptTradeDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ComplexResolveService {

	private final NamedParameterJdbcTemplate olapJdbc;

	/** 캐시들 (Batch 실행 동안 유지) */
	private final Map<String, String> aptSeqToComplexPkCache = new ConcurrentHashMap<>();
	private final Map<String, Boolean> aptSeqMissCache = new ConcurrentHashMap<>();

	private final Map<String, String> pnuToUniqueComplexPkCache = new ConcurrentHashMap<>();
	private final Map<String, Boolean> pnuMissCache = new ConcurrentHashMap<>();

	private final Map<String, String> pnuNameToComplexPkCache = new ConcurrentHashMap<>();
	private final Set<String> ambiguousLogged = ConcurrentHashMap.newKeySet();

	public ComplexResolveService(
		NamedParameterJdbcTemplate olapJdbc
	) {
		this.olapJdbc = olapJdbc;
	}

	public ResolvedComplex resolve(
		ApisAptTradeDto dto,
		String dealYmd
	) {

		String aptSeq = dto.aptSeq();
		if (hasText(aptSeq)) {
			String cached = aptSeqToComplexPkCache.get(aptSeq);
			if (cached != null) {
				return new ResolvedComplex(cached, ResolvePath.APTSEQ);
			}

			if (!aptSeqMissCache.containsKey(aptSeq)) {
				List<String> found = olapJdbc.queryForList(
					"select complex_pk from complex where apt_seq = :aptSeq limit 1",
					Map.of("aptSeq", aptSeq),
					String.class
				);

				if (!found.isEmpty()) {
					String complexPk = found.get(0);
					aptSeqToComplexPkCache.put(aptSeq, complexPk);
					return new ResolvedComplex(complexPk, ResolvePath.APTSEQ);
				}
				aptSeqMissCache.put(aptSeq, true);
			}
		}

		String pnu = buildPnu(dto);
		if (hasText(pnu)) {

			String cached = pnuToUniqueComplexPkCache.get(pnu);
			if (cached != null) {
				return new ResolvedComplex(cached, ResolvePath.PNU_UNIQUE);
			}

			if (!pnuMissCache.containsKey(pnu)) {
				List<ComplexCandidate> cands = queryCandidatesByPnu(pnu);

				if (cands.isEmpty()) {
					pnuMissCache.put(pnu, true);
				}
				else if (cands.size() == 1) {
					String complexPk = cands.get(0).complexPk();
					pnuToUniqueComplexPkCache.put(pnu, complexPk);
					return new ResolvedComplex(complexPk, ResolvePath.PNU_UNIQUE);
				}
				else {
					ComplexCandidate chosen = chooseByName(dto.aptNm(), cands);
					if (chosen != null) {
						pnuNameToComplexPkCache.put(pnuNameKey(pnu, dto.aptNm()), chosen.complexPk());
						return new ResolvedComplex(chosen.complexPk(), ResolvePath.PNU_NAME);
					}

					String logKey = pnu + "|" + dealYmd;
					if (ambiguousLogged.add(logKey)) {
						log.warn(
							"[RESOLVE][AMBIGUOUS] pnu={}, aptNm={}, candidates={}",
							pnu, dto.aptNm(), cands.size()
						);
					}
				}
			}
		}

		return null;
	}

	private List<ComplexCandidate> queryCandidatesByPnu(String pnu) {
		return olapJdbc.query(
			"""
			select c.complex_pk as complexPk,
			       c.trade_name as tradeName,
			       c.name as name
			  from complex c
			  left join parcel p on p.id = c.parcel_id
			 where c.pnu = :pnu or p.pnu = :pnu
			""",
			Map.of("pnu", pnu),
			(rs, rowNum) -> new ComplexCandidate(
				rs.getString("complexPk"),
				rs.getString("tradeName"),
				rs.getString("name")
			)
		);
	}

	private ComplexCandidate chooseByName(
		String aptNm,
		List<ComplexCandidate> cands
	) {
		if (!hasText(aptNm)) return null;

		String target = normalizeName(aptNm);

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
			}
			else if (score == bestScore) {
				tie = true;
			}
		}

		return (bestScore > 0 && !tie) ? best : null;
	}

	private int scoreName(String target, ComplexCandidate c) {
		String t1 = normalizeName(c.tradeName());
		String t2 = normalizeName(c.name());

		if (target.equals(t1) || target.equals(t2)) return 3;
		if (t1.contains(target) || target.contains(t1)) return 2;
		if (t2.contains(target) || target.contains(t2)) return 2;

		return 0;
	}

	private String normalizeName(String s) {
		if (!hasText(s)) return "";
		return s.replaceAll("\\s+", "")
			.replaceAll("[()\\[\\]{}.,·\\-_/]", "")
			.toLowerCase(Locale.ROOT);
	}

	private String buildPnu(ApisAptTradeDto dto) {
		if (!hasText(dto.sggCd()) || !hasText(dto.umdCd())) return null;
		if (dto.sggCd().length() != 5 || dto.umdCd().length() != 5) return null;

		String bon = digitsOnly(dto.bonbun());
		if (!hasText(bon)) return null;

		String bu = digitsOnly(dto.bubun());
		String land = (hasText(dto.jibun()) && dto.jibun().contains("산")) ? "2" : "1";

		return dto.sggCd()
			+ dto.umdCd()
			+ land
			+ pad4(bon)
			+ pad4(hasText(bu) ? bu : "0");
	}

	private String pad4(String s) {
		return "0".repeat(Math.max(0, 4 - s.length())) + s;
	}

	private String digitsOnly(String s) {
		if (!hasText(s)) return null;
		String d = s.replaceAll("[^0-9]", "");
		return d.isBlank() ? null : d;
	}

	private boolean hasText(String s) {
		return s != null && !s.isBlank();
	}

	private String pnuNameKey(String pnu, String aptNm) {
		return pnu + "|" + normalizeName(aptNm);
	}

	public record ResolvedComplex(String complexPk, ResolvePath path) {}
	public enum ResolvePath { APTSEQ, PNU_UNIQUE, PNU_NAME }
	private record ComplexCandidate(String complexPk, String tradeName, String name) {}
}
