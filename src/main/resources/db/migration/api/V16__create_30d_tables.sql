create table if not exists trade_top_volume_30d
(
    region_id   bigint not null references region(id),
    rank        int    not null,
    complex_id  bigint not null references complex(id),
    deal_count  int    not null,
    primary key (region_id, rank)
);

create table if not exists trade_top_price_30d
(
    rank        int    not null,
    complex_id  bigint not null references complex(id),
    max_price   bigint not null,
    primary key (rank)
);
