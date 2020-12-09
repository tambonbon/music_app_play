package models

import controllers.CreateAlbumForm
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Albums(id: Int, artist: String, name: String, genre: String)

object Albums {
  implicit val albumsFormat = Json.format[Albums]

  val albumsForm: Form[CreateAlbumForm] = Form (
    mapping(
      "artist" -> text,
      "name" -> text,
      "genre" -> text,

    )(CreateAlbumForm.apply)(CreateAlbumForm.unapply)
  )

}