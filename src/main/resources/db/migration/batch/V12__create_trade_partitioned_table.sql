-- 1) sequence
CREATE SEQUENCE IF NOT EXISTS trade_id_seq
    START WITH 1
    INCREMENT BY 100
    CACHE 100;

-- 2) partitioned table
CREATE TABLE IF NOT EXISTS trade
(
    id          BIGINT       NOT NULL DEFAULT nextval('trade_id_seq'),
    deal_date   DATE         NOT NULL,

    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,

    apt_dong    TEXT,
    deal_amount BIGINT       NOT NULL,
    floor       INTEGER,
    excl_area   DOUBLE PRECISION,

    complex_pk  VARCHAR(255) NOT NULL,
    apt_seq     VARCHAR(255),

    source      VARCHAR(30)  NOT NULL DEFAULT 'RTMS',
    source_key  VARCHAR(100),

    PRIMARY KEY (deal_date, id),
    CONSTRAINT uq_trade_nk UNIQUE (complex_pk, deal_date, floor, excl_area, deal_amount)
)
    PARTITION BY RANGE (deal_date);

-- 3) yearly partitions + default
DO $$
    DECLARE
  y INT;
start_date DATE;
end_date DATE;
part_name TEXT;
BEGIN
    FOR y IN 2010..2026 LOOP
    start_date := make_date(y, 1, 1);
end_date := make_date(y + 1, 1, 1);
part_name := format('trade_%s', y);

EXECUTE format(
      'CREATE TABLE IF NOT EXISTS %I PARTITION OF trade FOR VALUES FROM (%L) TO (%L);',
      part_name, start_date, end_date
    );
END LOOP;

EXECUTE 'CREATE TABLE IF NOT EXISTS trade_default PARTITION OF trade DEFAULT;';
END $$;

-- 4) indexes
CREATE INDEX IF NOT EXISTS ix_trade_complexpk_date
    ON trade (complex_pk, deal_date DESC);

CREATE INDEX IF NOT EXISTS ix_trade_active_date
    ON trade (deal_date)
    WHERE deleted_at IS NULL;

-- 5) sync state
CREATE TABLE IF NOT EXISTS trade_sync_state
(
    name VARCHAR(50) PRIMARY KEY,
    last_updated_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO trade_sync_state(name, last_updated_at)
VALUES ('WEB_TRADE_CURRENT', '1970-01-01T00:00:00Z')
ON CONFLICT (name) DO NOTHING;

CREATE INDEX IF NOT EXISTS ix_complex_apt_seq ON complex(apt_seq);
CREATE INDEX IF NOT EXISTS ix_parcel_pnu ON parcel(pnu);
CREATE INDEX IF NOT EXISTS ix_complex_pnu ON complex(pnu);
