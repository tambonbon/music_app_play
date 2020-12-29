package models

import play.api.libs.json.Json

case class PlayingSong(albumId: Int, songId:Int, playingId: Int)

object PlayingSong {
  implicit val playingSongFormat = Json.format[PlayingSong]
}
