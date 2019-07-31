-- !Ups

insert into offsets(id, created_at, offset_type, value)
values ('a03dcb5c-939b-4112-b02c-ebb9755f7ae9', now(), 'ForgotPassword', 1);

alter table reset_password_tokens add index bigserial;

-- !Downs

alter table reset_password_tokens drop index;

delete from offsets where id = 'a03dcb5c-939b-4112-b02c-ebb9755f7ae9';