# --- !Ups
drop table "albums";

create table "albums" (
  "id" int not null primary key,
  "artist" varchar not null,
  "name"   varchar not null,
  "genre"   varchar not null
);
create table "songs" (
  "id" int not null primary key,
  "title" varchar not null,
  "duration" varchar not null
);

# --- !Downs

drop table "albums" if exists;
drop table "songs" if exists;