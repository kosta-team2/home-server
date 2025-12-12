-- 1) 동일 PNU로 parcel 매핑
UPDATE complex AS c
SET parcel_id = p.id
FROM parcel AS p
WHERE c.pnu = p.pnu;

-- 2) 매핑 실패한 complex 확인 (디버깅용)
SELECT c.id, c.complex_pk, c.trade_name, c.pnu
FROM complex c
WHERE c.parcel_id IS NULL;
