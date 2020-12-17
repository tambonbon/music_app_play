# --- !Ups

create table "playing" (
  "playingId" int generated by default as identity(start with 1) not null primary key,
  "artist" varchar not null,
  "song"   varchar not null
);

# --- !Downs

drop table "playing" if exists;
