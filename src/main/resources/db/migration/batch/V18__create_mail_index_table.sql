create index if not exists ix_mail_target_pick
    on public.mail_target(batch_date, status, id);

create unique index if not exists ux_mail_target
    on public.mail_target(batch_date, mail_type, user_id, parcel_id);
