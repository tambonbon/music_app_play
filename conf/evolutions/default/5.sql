# --- !Ups

create table "album_song" (
  "albumID" int not null ,
  "songID" int not null
);

# --- !Downs

drop table "album_song" if exists;
