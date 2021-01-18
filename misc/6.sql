# --- !Ups

ALTER TABLE "songs" RENAME COLUMN "id" to "songId";

# --- !Downs

drop table "songs" if exists;

