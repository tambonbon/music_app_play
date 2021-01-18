# --- !Ups

create table "albums" (
  "artist" varchar not null primary key,
  "name"   varchar not null,
  "genre"   varchar not null,
  "title"  varchar not null,
  "duration"  varchar not null
);

# --- !Downs

drop table "albums" if exists;