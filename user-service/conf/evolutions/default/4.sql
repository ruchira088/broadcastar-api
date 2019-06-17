-- !Ups

create table reset_password_tokens(
    user_id uuid not null,
    secret uuid not null,
    created_at timestamp not null,
    email varchar(255) not null,
    expires_at timestamp not null,
    reset_at timestamp,
    primary key (user_id, secret)
);

-- !Downs

drop table reset_password_tokens;
