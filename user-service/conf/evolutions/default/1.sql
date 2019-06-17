-- !Ups

create table users(
    user_id uuid,
    created_at timestamp not null,
    index bigserial,
    username varchar(255) not null unique,
    first_name varchar(255) not null,
    last_name varchar(255),
    email varchar(255) not null unique,
    password varchar(2047) not null,
    profile_image_id varchar(255),
    email_verified boolean not null,
    primary key (user_id)
);

-- !Downs

drop table users;
