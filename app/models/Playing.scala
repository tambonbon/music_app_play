package models

import play.api.libs.json.Json

case class Playing(playingId: Int, artist: String, song: String)
case class CreatePlayingForm(artist: String, song: String)

object Playing  {
  implicit val playingFormat = Json.format[Playing]
}
