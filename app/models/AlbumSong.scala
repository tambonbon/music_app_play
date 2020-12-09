package models

import play.api.libs.json.Json

case class AlbumSong(albumID: Int, songID: Int)

object AlbumSong {
  implicit val albumSongFormat = Json.format[AlbumSong]
}
