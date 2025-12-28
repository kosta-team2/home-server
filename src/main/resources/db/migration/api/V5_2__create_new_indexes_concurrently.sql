# CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trade_latest_by_complex
#     ON trade (complex_id, deal_date DESC, id DESC)
#     INCLUDE (deal_amount, excl_area)
#     WHERE deleted_at IS NULL;
#
# CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_complex_active_by_parcel
#     ON complex (parcel_id, id)
#     WHERE deleted_at IS NULL;
#
# CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_complex_active_parcel_usedate
#     ON complex (parcel_id, use_date)
#     WHERE deleted_at IS NULL AND use_date IS NOT NULL;
