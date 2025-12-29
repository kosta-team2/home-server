alter table region
    add column if not exists trend_30d double precision;
