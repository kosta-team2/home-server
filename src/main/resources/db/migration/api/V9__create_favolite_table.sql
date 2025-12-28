CREATE
SEQUENCE IF NOT EXISTS favorite_parcel_id_seq
    START
WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE
50;


CREATE TABLE IF NOT EXISTS favorite_parcel
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('favorite_parcel_id_seq'),

    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMP WITHOUT TIME ZONE,

    user_id       BIGINT  NOT NULL,
    parcel_id     BIGINT  NOT NULL,

    complex_name  TEXT    NOT NULL,
    alarm_enabled BOOLEAN NOT NULL   DEFAULT true,

    CONSTRAINT fk_favorite_parcel_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_favorite_parcel_parcel
        FOREIGN KEY (parcel_id)
            REFERENCES parcel (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_favorite_user_parcel
        UNIQUE (user_id, parcel_id)
);
