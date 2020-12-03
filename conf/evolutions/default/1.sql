# --- !Ups

create table "users" (
  "username" varchar not null primary key,
  "password" varchar not null
);

# --- !Downs

drop table "users" if exists;