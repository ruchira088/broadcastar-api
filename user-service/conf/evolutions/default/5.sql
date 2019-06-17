-- !Ups

create table resource_information(
    key varchar(300),
    created_at timestamp not null,
    file_name varchar(255) not null,
    content_type varchar(255),
    file_size bigint not null,
    primary key (key)
);

-- !Downs

drop table resource_information;
