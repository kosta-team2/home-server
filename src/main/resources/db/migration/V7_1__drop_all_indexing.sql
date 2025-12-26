-- region
DROP INDEX IF EXISTS idx_region_parent_id;
DROP INDEX IF EXISTS idx_region_sgg_emd;
DROP INDEX IF EXISTS idx_region_level_lat_lng;

-- parcel
DROP INDEX IF EXISTS idx_parcel_region_id;
DROP INDEX IF EXISTS idx_parcel_alive_lat_lng;

-- complex
DROP INDEX IF EXISTS idx_complex_active_by_parcel;
DROP INDEX IF EXISTS idx_complex_active_parcel_usedate;

-- trade
DROP INDEX IF EXISTS idx_trade_latest_by_complex;
