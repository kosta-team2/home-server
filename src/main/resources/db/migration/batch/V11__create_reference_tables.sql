-- region
CREATE SEQUENCE IF NOT EXISTS region_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS region
(
    id        BIGINT PRIMARY KEY DEFAULT nextval('region_id_seq'),
    sgg_code  VARCHAR(5)  NOT NULL,
    emd_code  VARCHAR(5),
    level     VARCHAR(20) NOT NULL,
    name      TEXT        NOT NULL,
    full_name TEXT,
    longitude DOUBLE PRECISION,
    latitude  DOUBLE PRECISION,
    parent_id BIGINT REFERENCES region (id)
);

CREATE INDEX IF NOT EXISTS ix_region_level_sgg
    ON region (level, sgg_code);

-- raw schema + parcel_raw
CREATE SCHEMA IF NOT EXISTS raw;

CREATE TABLE IF NOT EXISTS raw.parcel_raw
(
    pnu       VARCHAR(19) PRIMARY KEY,
    longitude DOUBLE PRECISION,
    latitude  DOUBLE PRECISION
);

-- parcel
CREATE SEQUENCE IF NOT EXISTS parcel_id_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 100;

CREATE TABLE IF NOT EXISTS parcel
(
    id         BIGINT PRIMARY KEY                   DEFAULT nextval('parcel_id_seq'),

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,

    pnu        VARCHAR(19)                 NOT NULL,
    longitude  DOUBLE PRECISION,
    latitude   DOUBLE PRECISION,
    address    TEXT                        NOT NULL,

    region_id  BIGINT REFERENCES region (id),

    CONSTRAINT uq_parcel_pnu UNIQUE (pnu)
);

-- complex
CREATE SEQUENCE IF NOT EXISTS complex_id_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 100;

CREATE TABLE IF NOT EXISTS complex
(
    id         BIGINT PRIMARY KEY                   DEFAULT nextval('complex_id_seq'),

    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,

    apt_seq    VARCHAR(255),
    complex_pk VARCHAR(255)                NOT NULL,
    pnu        VARCHAR(19),
    trade_name TEXT                        NOT NULL,
    name       TEXT                        NOT NULL,
    dong_cnt   INTEGER,
    unit_cnt   INTEGER,
    plat_area  DOUBLE PRECISION,
    arch_area  DOUBLE PRECISION,
    tot_area   DOUBLE PRECISION,
    bc_rat     DOUBLE PRECISION,
    vl_rat     DOUBLE PRECISION,
    use_date   DATE,

    parcel_id  BIGINT REFERENCES parcel (id),

    CONSTRAINT uq_complex_apt_seq UNIQUE (apt_seq),
    CONSTRAINT uq_complex_pk UNIQUE (complex_pk)
);

CREATE INDEX IF NOT EXISTS ix_complex_apt_seq ON complex (apt_seq);
