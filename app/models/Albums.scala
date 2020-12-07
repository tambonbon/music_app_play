package models

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Albums(artist: String, name: String, genre: String, songs: Songs)

object Albums {
  implicit val albumsFormat = Json.format[Albums]

  val albumsForm: Form[Albums] = Form (
    mapping(
      "artist" -> text,
      "name" -> text,
      "genre" -> text,
      "songs" -> //seq(
        mapping(
          "title" -> text,
          "duration" -> text
        )(Songs.apply)(Songs.unapply)
      // )
    )(Albums.apply)(Albums.unapply)
  )

}