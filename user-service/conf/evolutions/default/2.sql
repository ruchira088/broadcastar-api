-- !Ups

create table authentication_tokens(
    session_token varchar(255),
    user_id uuid not null,
    secret_token varchar(255) not null,
    created_at timestamp not null,
    expires_at timestamp not null,
    primary key (secret_token)
);

-- !Downs

drop table authentication_tokens;
