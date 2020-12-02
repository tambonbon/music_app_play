FROM adoptopenjdk:15-jre
COPY ./music_app_play-1.0-SNAPSHOT /music_app_play-1.0-SNAPSHOT
EXPOSE 9000
CMD music_app_play-1.0-SNAPSHOT/bin/music_app_play