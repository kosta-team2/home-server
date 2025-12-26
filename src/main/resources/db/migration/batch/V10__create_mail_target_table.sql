create table if not exists mail_target
(
    id bigserial primary key,
    batch_date   date         not null,
    mail_type    varchar(30)  not null, -- 예: TRADE_UPDATE
    user_id      bigint       not null,
    parcel_id    bigint       not null,
    email        varchar(255) not null,
    complex_name varchar(255) not null,
    status       varchar(20)  not null, -- PENDING / PROCESSING / SENT / FAILED
    try_count    int          not null default 0,
    last_error   text,
    sent_at timestamptz,
    created_at timestamptz not null default now()
);

-- 같은 날짜에 같은 사용자/필지에 대해 중복 타깃 생성 방지
create unique index if not exists ux_mail_target
    on mail_target (batch_date, mail_type, user_id, parcel_id);

create index if not exists ix_mail_target_pick
    on mail_target (batch_date, status, id);
