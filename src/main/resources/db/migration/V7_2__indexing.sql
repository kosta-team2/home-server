-- region (부모id를 기준으로 정렬 지역 선택 시 속도 향상)
CREATE INDEX IF NOT EXISTS idx_region_parent_id
    ON region (parent_id);

-- [지역 통계1] 시군구, 읍면동 기준으로 정렬 (지역별 통계, 집계시)
-- region
CREATE INDEX IF NOT EXISTS idx_region_sgg_emd
    ON region (sgg_code, emd_code);

-- parcel
CREATE INDEX IF NOT EXISTS idx_parcel_region_alive
    ON parcel(region_id)
    WHERE deleted_at IS NULL;

-- complex
CREATE INDEX IF NOT EXISTS idx_complex_alive_parcel_id
    ON complex (parcel_id)
    WHERE deleted_at IS NULL;

-- region PostGis
ALTER TABLE region ADD COLUMN geom geometry(Point, 4326);

-- lat, lng가 이상하게 region에 들어가는 것은 삽입전에 예외처리해줄 것(null or 튀는 값)
UPDATE region
SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE geom IS NULL;

-- region 추가 시 트리거
CREATE OR REPLACE FUNCTION region_set_geom()
RETURNS trigger AS $$
BEGIN
  IF NEW.longitude IS NOT NULL AND NEW.latitude IS NOT NULL THEN
    NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
ELSE
    NEW.geom := NULL;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_region_set_geom ON region;

CREATE TRIGGER trg_region_set_geom
    BEFORE INSERT OR UPDATE OF longitude, latitude ON region
    FOR EACH ROW
    EXECUTE FUNCTION region_set_geom();

-- 인덱싱
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_region_geom
    ON region USING GIST (geom);

-------- parcel
-- postgis
ALTER TABLE parcel
    ADD COLUMN geom geometry(Point, 4326);

UPDATE parcel
SET geom = ST_SetSRID(
        ST_MakePoint(longitude, latitude),
        4326
           )
WHERE geom IS NULL;

CREATE INDEX IF NOT EXISTS idx_parcel_geom_alive
    ON parcel
    USING GIST (geom)
    WHERE deleted_at IS NULL;

CREATE OR REPLACE FUNCTION parcel_set_geom()
RETURNS trigger AS $$
BEGIN
  IF NEW.latitude IS NULL OR NEW.longitude IS NULL THEN
    NEW.geom := NULL;
RETURN NEW;
END IF;

  NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

------------
-- trade
-- complex_id별로 가장 최신 row 1개를 ORDER BY deal_date DESC, id DESC LIMIT 1로 찾을 때 최적
CREATE INDEX IF NOT EXISTS idx_trade_latest_by_complex
    ON trade (complex_id, deal_date DESC, id DESC)
    INCLUDE (deal_amount, excl_area)
    WHERE deleted_at IS NULL;

-- complex를 빠르게 조회 ( parcel -> trade 찾기)
CREATE INDEX IF NOT EXISTS idx_complex_alive_by_parcel
    ON complex (parcel_id, id)
    WHERE deleted_at IS NULL;

-- parcel 기본 정보를 찾기 위해 use_date가 오래된 것을 빠르게 찾기
CREATE INDEX IF NOT EXISTS idx_complex_alive_parcel_use_date
    ON complex (parcel_id, use_date)
    WHERE deleted_at IS NULL AND use_date IS NOT NULL;

