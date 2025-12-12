-- 1) 읍면동(EUP_MYEON_DONG) 단위로 매핑
UPDATE parcel AS p
SET region_id = r.id
FROM region AS r
WHERE r.level = 'EUP_MYEON_DONG'
  AND r.sgg_code = SUBSTRING(p.pnu, 1, 5)
  AND r.emd_code = SUBSTRING(p.pnu, 6, 3) || '00';

-- 2) 남은 것들 SIGUNGU 레벨로 매핑
UPDATE parcel AS p
SET region_id = r.id
FROM region AS r
WHERE p.region_id IS NULL
  AND r.level = 'SIGUNGU'
  AND r.sgg_code = SUBSTRING(p.pnu, 1, 5);

-- 3) 그래도 남은 것은 SIDO로 fallback
UPDATE parcel AS p
SET region_id = r.id
FROM region AS r
WHERE p.region_id IS NULL
  AND r.level = 'SIDO'
  AND SUBSTRING(p.pnu, 1, 2) = SUBSTRING(r.sgg_code, 1, 2);
