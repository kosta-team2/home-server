ALTER TABLE region
    ADD COLUMN IF NOT EXISTS trend_30d double precision;
