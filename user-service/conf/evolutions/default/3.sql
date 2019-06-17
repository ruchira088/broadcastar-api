-- !Ups

create table offsets(
    id uuid,
    created_at timestamp not null,
    offset_type varchar(255) not null,
    value bigint not null,
    lock_acquired_at timestamp,
    primary key (id)
);

insert into offsets(id, created_at, offset_type, value)
values ('ab5abe5a-da88-42f8-90ab-e7656bd97bed', now(), 'UserCreated', 1);

-- !Downs

drop table offsets;
