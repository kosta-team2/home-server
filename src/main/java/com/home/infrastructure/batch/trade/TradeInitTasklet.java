package com.home.infrastructure.batch.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.home.domain.complex.Complex;
import com.home.domain.complex.ComplexRepository;
import com.home.domain.region.Region;
import com.home.domain.region.RegionLevel;
import com.home.domain.region.RegionRepository;
import com.home.domain.trade.Trade;
import com.home.domain.trade.TradeRepository;
import com.home.infrastructure.external.apis.ApisClient;
import com.home.infrastructure.external.apis.dto.ApisAptTradeDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class TradeInitTasklet implements Tasklet {

	private static final int PAGE_SIZE = 1000;
	private static final int BATCH_SIZE = 500;

	private static final String EC_READ = "trade.read";
	private static final String EC_SAVED = "trade.saved";

	private final ApisClient apisClient;
	private final RegionRepository regionRepository;
	private final ComplexRepository complexRepository;
	private final TradeRepository tradeRepository;

	@Value("#{jobParameters['dealYmd']}")
	private String dealYmd;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		ExecutionContext ec = chunkContext.getStepContext().getStepExecution().getExecutionContext();
		long read = ec.containsKey(EC_READ) ? ec.getLong(EC_READ) : 0L;
		long saved = ec.containsKey(EC_SAVED) ? ec.getLong(EC_SAVED) : 0L;

		List<Region> regionList = regionRepository.findAllByLevel(RegionLevel.SIGUNGU);
		List<Trade> batchList = new ArrayList<>(BATCH_SIZE);

		for (Region region : regionList) {
			int pageNo = 1;

			while (true) {
				List<ApisAptTradeDto> apisList = apisClient.getAptTrade(
					pageNo,
					PAGE_SIZE,
					region.getSggCode(),
					dealYmd
				).toApisDto();

				if (apisList == null || apisList.isEmpty()) {
					break;
				}

				for (ApisAptTradeDto dto : apisList) {
					read++;
					Optional<Complex> complexOpt = complexRepository.findByAptSeq(dto.aptSeq());

					if (complexOpt.isEmpty()) {
						List<Complex> list = complexRepository.findByTradeName(dto.aptNm());

						if (list.isEmpty()) {
							log.warn("[BATCH][tradeInit] 단지 매핑 실패. aptSeq={}, aptNm={}. Trade 스킵",
								dto.aptSeq(), dto.aptNm());
							continue;
						}

						String targetEmd = dto.umdCd();

						complexOpt = list.stream()
							.filter(c -> {
								if (c.getParcel() == null || c.getParcel().getPnu() == null)
									return false;
								String pnu = c.getParcel().getPnu();
								if (pnu.length() < 10)
									return false;
								return pnu.substring(5, 10).equals(targetEmd);
							})
							.findFirst();

						if (complexOpt.isEmpty()) {
							log.warn("[BATCH][tradeInit] 같은 이름 단지 여러 개지만 법정동(PNU) 코드 일치 없음. aptNm={}, umdCd={}",
								dto.aptNm(), targetEmd);
							continue;
						}

						complexOpt.ifPresent(complex -> complex.updateAptSeq(dto.aptSeq()));
					}

					Trade trade = Trade.create(
						dto.aptDong(),
						dto.dealAmount(),
						dto.dealDay(),
						dto.dealMonth(),
						dto.dealYear(),
						dto.floor(),
						dto.exclArea(),
						complexOpt.get()
					);

					batchList.add(trade);

					if (batchList.size() >= BATCH_SIZE) {
						tradeRepository.saveAll(batchList);
						saved += batchList.size();
						batchList.clear();
					}
				}

				if (apisList.size() < PAGE_SIZE) {
					break;
				}
				pageNo++;
			}
		}

		if (!batchList.isEmpty()) {
			tradeRepository.saveAll(batchList);
			saved += batchList.size();
			batchList.clear();
		}

		ec.putLong(EC_READ, read);
		ec.putLong(EC_SAVED, saved);

		return RepeatStatus.FINISHED;
	}
}
