CREATE INDEX IF NOT EXISTS idx_parcel_region_alive
    ON parcel(region_id)
    WHERE deleted_at IS NULL;

-- postgis
ALTER TABLE parcel
    ADD COLUMN IF NOT EXISTS geom geometry(Point, 4326);

UPDATE parcel
SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE geom IS NULL;

-- index
CREATE INDEX IF NOT EXISTS idx_parcel_geom_alive
    ON parcel USING GIST (geom)
    WHERE deleted_at IS NULL AND geom IS NOT NULL;

-- trigger
CREATE OR REPLACE FUNCTION parcel_set_geom()
    RETURNS trigger AS $$
BEGIN
    IF NEW.longitude IS NULL OR NEW.latitude IS NULL THEN
        NEW.geom := NULL;
    ELSE
        NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_parcel_set_geom ON parcel;

CREATE TRIGGER trg_parcel_set_geom
    BEFORE INSERT OR UPDATE OF longitude, latitude ON parcel
    FOR EACH ROW
EXECUTE FUNCTION parcel_set_geom();
