CREATE SEQUENCE IF NOT EXISTS user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('user_id_seq'),

    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMP WITHOUT TIME ZONE,

    user_name     VARCHAR(255) NOT NULL,
    display_name  VARCHAR(50)  NOT NULL,
    user_email    VARCHAR(255) NOT NULL,
    profile_image TEXT         NOT NULL,

    role          VARCHAR(50)  NOT NULL,
    type          VARCHAR(50)  NOT NULL,
    provider_id   VARCHAR(255) NOT NULL
);

-- soft delete 조회 성능용
CREATE INDEX idx_users_deleted_at
    ON users (deleted_at);

-- OAuth 사용자 조회용
CREATE INDEX idx_users_provider
    ON users (type, provider_id);


CREATE TABLE IF NOT EXISTS refresh_token
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    token_hash  VARCHAR(128) NOT NULL,

    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at  TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- 사용자별 토큰 조회
CREATE INDEX idx_refresh_token_user_id
    ON refresh_token (user_id);

-- 토큰 검증 / 만료 정리
CREATE INDEX idx_refresh_token_valid
    ON refresh_token (token_hash, expires_at);

-- 1 유저 = 1 토큰 전략
CREATE UNIQUE INDEX uq_refresh_token_user
    ON refresh_token (user_id)
    WHERE revoked_at IS NULL;
