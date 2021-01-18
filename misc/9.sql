# --- !Ups

ALTER TABLE "playing" ADD "user" varchar;

# --- !Downs

drop table "playing" if exists;
