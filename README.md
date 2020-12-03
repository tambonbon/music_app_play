# Music App using Play + Slick

Type `sbt run` to start the app. 

To run in production mode, first use `sbt dist` to create a .zip distribution.
Afterwards do `unzip -o target/universe/music_app_play-1.0-SNAPSHOT` to make sure that any
existing distribution is overwritten. Then you can run dockerfile or use `docker-compose up` to start 
deploying.

The host is localhost:9000  