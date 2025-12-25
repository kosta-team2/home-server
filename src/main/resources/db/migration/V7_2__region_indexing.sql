CREATE INDEX IF NOT EXISTS idx_region_parent_id
    ON region (parent_id);

-- 시군구, 읍면동 기준으로 정렬 (지역별 통계, 집계시)
CREATE INDEX IF NOT EXISTS idx_region_sgg_emd
    ON region (sgg_code, emd_code);

-- region PostGis
ALTER TABLE region ADD COLUMN geom geometry(Point, 4326);

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
