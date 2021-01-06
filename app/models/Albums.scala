package models

import controllers.CreateAlbumForm
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes, __}

case class Albums(id: Int, artist: String, name: String, genre: String)

object Albums {
  implicit val albumsWrites: Writes[Albums] = Writes { album =>
    Json.obj(
      "artist" -> album.artist,
      "name" -> album.name,
      "genre" -> album.genre
    )
  }

  implicit val albumReads: Reads[Albums] =  (
    (__ \ "id").read[Int] and
    (__ \ "artist").read[String] and
    (__ \ "name").read[String] and
    (__ \ "genre").read[String]
  )(Albums.apply _)

  val albumsForm: Form[CreateAlbumForm] = Form (
    mapping(
      "artist" -> text,
      "name" -> text,
      "genre" -> text,

    )(CreateAlbumForm.apply)(CreateAlbumForm.unapply)
  )

}