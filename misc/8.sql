# --- !Ups

create table "playing_song" (
  "albumId" int not null ,
  "songId" int not null,
  "playingId" int not null
);

# --- !Downs

drop table "playing_song" if exists;