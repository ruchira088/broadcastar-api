-- !Ups

create table email_verification_tokens(
    user_id uuid not null,
    secret uuid not null,
    email varchar(255) not null,
    created_at timestamp not null,
    verified_at timestamp,
    primary key (user_id, secret)
);

-- !Downs

drop table email_verification_tokens;
