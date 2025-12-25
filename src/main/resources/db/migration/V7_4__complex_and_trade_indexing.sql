CREATE INDEX IF NOT EXISTS idx_complex_alive_by_parcel_id
    ON complex (parcel_id, id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_complex_alive_parcel_use_date
    ON complex (parcel_id, use_date)
    WHERE deleted_at IS NULL AND use_date IS NOT NULL;

-- 최신 거래 1개 인덱싱 / 비정규화시 인덱스 제거
CREATE INDEX IF NOT EXISTS idx_trade_latest_by_complex
    ON trade (complex_id, deal_date DESC, id DESC)
    INCLUDE (deal_amount, excl_area)
    WHERE deleted_at IS NULL;
