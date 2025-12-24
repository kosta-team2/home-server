-- trade 연도별 partitioning
CREATE TABLE trade (
                       id           bigint GENERATED ALWAYS AS IDENTITY,
                       complex_id   bigint NOT NULL,
                       deal_date    date   NOT NULL,
                       deal_amount  bigint,
                       excl_area    numeric,
                       deleted_at   timestamptz,
    -- ✅ 파티션 테이블에서는 보통 PK/UK에 파티션 키(deal_date)가 포함돼야 함
                       PRIMARY KEY (id, deal_date)
)
    PARTITION BY RANGE (deal_date);

------

CREATE TABLE trade_2023 PARTITION OF trade
    FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');

CREATE TABLE trade_2024 PARTITION OF trade
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE trade_2025 PARTITION OF trade
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE trade_2026 PARTITION OF trade
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE trade_default PARTITION OF trade DEFAULT;

----------

-- 최근 3년 조회/최신값 조회에 흔한 패턴
CREATE INDEX idx_trade_complex_dealdate_desc
    ON trade (complex_id, deal_date DESC, id DESC);

-- deleted_at 조건을 항상 쓰면 부분 인덱스도 가능 (단 파티션드 인덱스 지원 버전 확인 필요)
-- CREATE INDEX ... ON trade (...) WHERE deleted_at IS NULL;
CREATE INDEX ON trade_2025 (complex_id, deal_date DESC, id DESC);
