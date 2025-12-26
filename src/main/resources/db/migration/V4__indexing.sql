-- region
CREATE INDEX IF NOT EXISTS idx_region_parent_id
    ON region (parent_id);

CREATE INDEX IF NOT EXISTS idx_region_sgg_emd
    ON region (sgg_code, emd_code);

CREATE INDEX IF NOT EXISTS idx_region_level_lat_lng
    ON region (level, latitude, longitude);

-- parcel
CREATE INDEX IF NOT EXISTS idx_parcel_region_id
    ON parcel (region_id);

CREATE INDEX IF NOT EXISTS idx_parcel_alive_lat_lng
    ON parcel (latitude, longitude)
    WHERE deleted_at IS NULL;

-- complex
CREATE INDEX IF NOT EXISTS idx_complex_alive_parcel_id
    ON complex (parcel_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_complex_alive_parcel_use_date
    ON complex (parcel_id, use_date)
    WHERE deleted_at IS NULL;

-- trade
CREATE INDEX IF NOT EXISTS idx_trade_alive_complex_dealdate_id_desc
    ON trade (complex_id, deal_date DESC, id DESC)
    WHERE deleted_at IS NULL;
