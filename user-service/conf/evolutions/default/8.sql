-- !Ups

insert into offsets(id, created_at, offset_type, value)
values ('4de5e5ca-1daa-49b6-82b9-11be82396895', now(), 'EmailVerification', 1);

alter table email_verification_tokens add index bigserial;

-- !Downs

delete from offsets where id = '4de5e5ca-1daa-49b6-82b9-11be82396895';

alter table email_verification_tokens drop index;