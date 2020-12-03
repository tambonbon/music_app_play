package models

import play.api.libs.json.Json

case class Albums(artist: String, name: String, genre: String, songs: Seq[Songs])

object Albums {
  implicit val albumsFormat = Json.format[Albums]
}